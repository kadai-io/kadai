#!/usr/bin/env python3
"""
submit_software.py — Programmatically submit Software for measurement via the GMT API.

Features:
- List available machines
- Submit software to /v1/software/add

Env vars (optional):
  GMT_API_URL        -> base API URL will defailt to https://api.green-coding.io/
  GMT_AUTH_TOKEN     -> token for X-Authentication
  GMT_REMOVE_IDLE    -> "true" to append ?remove_idle=true to GET requests

Usage examples:
  # List machines (active only)
  python submit_software.py list-machines

  # Submit a one-off run
  python submit_software.py --token YOUR_TOKEN submit \
      --name "My App" \
      --repo-url "https://github.com/your/repo" \
      --machine-id 42 \
      --schedule-mode one-off \
      --email you@example.com \
      --filename usage_scenario.yml \
      --branch main \
      --variables "__GMT_VAR_API_KEY=abc123" \
      --variables "__GMT_VAR_DATABASE_URL=postgres://localhost"
"""

from __future__ import annotations

import argparse
import json
import os
import sys
from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Tuple, Union
import requests

# ---- Constants mirrored from your HTML's <select> options ----
VALID_SCHEDULE_MODES = {
    "one-off",
    "variance",
    "daily",
    "weekly",
    "commit",
    "commit-variance",
    "tag",
    "tag-variance",
    "statistical-significance",
}

DEFAULT_TIMEOUT = 30  # seconds


class APIEmptyResponse204(Exception):
    """Raised when API returns HTTP 204 (No Content) and we attempted to parse JSON."""
    pass


class APIError(Exception):
    """Raised when API returns success != true or an HTTP error."""
    pass


@dataclass
class APIClient:
    api_url: str
    token: Optional[str] = None
    remove_idle: bool = False
    timeout: int = DEFAULT_TIMEOUT

    def _auth_headers(self) -> Dict[str, str]:
        headers: Dict[str, str] = {"Content-Type": "application/json"}
        if self.token:
            headers["X-Authentication"] = self.token
        return headers

    def _merge_query_flag(self, path: str) -> str:
        if self.remove_idle:
            sep = "&" if "?" in path else "?"
            return f"{path}{sep}remove_idle=true"
        return path

    def _request(
        self,
        path: str,
        method: str = "GET",
        json_body: Optional[Dict[str, Any]] = None,
    ) -> Optional[Dict[str, Any]]:
        url = self.api_url.rstrip("/") + path
        if method.upper() == "GET":
            url = self._merge_query_flag(url)

        resp = requests.request(
            method=method.upper(),
            url=url,
            json=json_body if json_body is not None else None,
            headers=self._auth_headers() if method.upper() != "GET" else self._auth_headers() | {},
            timeout=self.timeout,
        )

        # Mirror your JS behavior for 204/202
        if resp.status_code == 204:
            raise APIEmptyResponse204("No data to display. API returned empty response (HTTP 204)")
        if resp.status_code == 202:
            return None  # Accepted, usually no body

        # raise for non-2xx (so 4xx/5xx are caught)
        try:
            resp.raise_for_status()
        except requests.HTTPError as e:
            # Try to parse error body when possible
            try:
                data = resp.json()
            except Exception:
                raise APIError(f"HTTP {resp.status_code}: {resp.text}") from e
            err = data.get("err", data)
            raise APIError(f"HTTP {resp.status_code}: {err}") from e

        # Parse JSON
        try:
            data = resp.json()
        except ValueError as e:
            raise APIError(f"Expected JSON but got: {resp.text[:200]}...") from e

        # Match your JS: check for success flag and err field(s)
        if isinstance(data, dict) and data.get("success") is not True:
            err = data.get("err")
            if isinstance(err, list) and err:
                # Prefer first message if present
                msg = (err[0].get("msg") if isinstance(err[0], dict) else str(err[0])) or str(err)
                raise APIError(msg)
            raise APIError(str(err))
        return data

    # ---- Public API wrappers ----

    def list_machines(self) -> List[Tuple[Any, str, bool]]:
        """
        Returns list of (id, name, active) tuples.
        The backend seems to encode machines as arrays: [id, name, active_bool, ...]
        """
        data = self._request("/v1/machines", method="GET")
        if not data or "data" not in data:
            return []
        result = []
        for machine in data["data"]:
            # Defensive parsing
            mid = machine[0] if len(machine) > 0 else None
            name = machine[1] if len(machine) > 1 else ""
            active = bool(machine[2]) if len(machine) > 2 else False
            result.append((mid, name, active))
        return result

    def submit_software(self, payload: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        """
        Submits software to /v1/software/add
        """
        # Trim string values like your JS
        for k, v in list(payload.items()):
            if isinstance(v, str):
                payload[k] = v.strip()

        return self._request("/v1/software/add", method="POST", json_body=payload)


def load_token(token_flag: Optional[str]) -> Optional[str]:
    """
    1) --token flag
    2) GMT_AUTH_TOKEN env
    3) ~/.gmt/token (optional convenience)
    """
    if token_flag:
        return token_flag.strip()
    env = os.getenv("GMT_AUTH_TOKEN")
    if env:
        return env.strip()
    # Optional: read from file
    token_path = os.path.expanduser("~/.gmt/token")
    if os.path.isfile(token_path):
        try:
            with open(token_path, "r", encoding="utf-8") as f:
                return f.read().strip()
        except Exception:
            pass
    return None


def bool_env(value: Optional[str]) -> bool:
    return (value or "").strip().lower() in {"1", "true", "yes", "y", "on"}


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(description="Submit ScenarioRunner measurements via API.")
    p.add_argument("--api-url", default=os.getenv("GMT_API_URL", "https://api.green-coding.io/").strip(),
                   help="Base API URL")
    p.add_argument("--token", default=None, help="X-Authentication token (or set GMT_AUTH_TOKEN)")
    p.add_argument("--remove-idle", action="store_true",
                   default=bool_env(os.getenv("GMT_REMOVE_IDLE")),
                   help="Append ?remove_idle=true to GET requests.")
    p.add_argument("--timeout", type=int, default=DEFAULT_TIMEOUT,
                   help=f"HTTP timeout in seconds (default {DEFAULT_TIMEOUT}).")
    p.add_argument("--json", action="store_true",
                   help="Print raw JSON responses.")

    sub = p.add_subparsers(dest="command", required=True)

    # list-machines
    s_list = sub.add_parser("list-machines", help="List active machines.")
    s_list.add_argument("--all", action="store_true",
                        help="Show all machines (including inactive).")

    # submit
    s_sub = sub.add_parser("submit", help="Submit software for measurement.")
    s_sub.add_argument("--name", required=True, help="Human-readable name.")
    s_sub.add_argument("--email", help="Optional email for completion notification.")
    s_sub.add_argument("--repo-url", dest="repo_url", required=True,
                       help="Repository URL (GitHub/GitLab/Bitbucket).")
    s_sub.add_argument("--filename", help="Optional usage scenario path (default usage_scenario.yml).")
    s_sub.add_argument("--branch", help="Optional branch (default main).")
    s_sub.add_argument("--machine-id", dest="machine_id", required=True,
                       help="Target machine ID (use list-machines to discover).")
    s_sub.add_argument("--schedule-mode", dest="schedule_mode", required=True,
                       choices=sorted(VALID_SCHEDULE_MODES),
                       help="Measurement schedule mode.")
    s_sub.add_argument("--variables", action="append", metavar="KEY=VALUE",
                       help="Usage scenario variables (can be used multiple times).")

    return p



def main():
    parser = build_parser()
    args = parser.parse_args()
    token = load_token(args.token)

    client = APIClient(
        api_url=args.api_url,
        token=token,
        remove_idle=getattr(args, "remove_idle", False),
        timeout=getattr(args, "timeout", DEFAULT_TIMEOUT),
    )

    try:
        if args.command == "list-machines":
            machines = client.list_machines()
            if not machines:
                print("No machines returned.")
                return

            rows = []
            for mid, name, active in machines:
                if args.all or active:
                    rows.append({"id": mid, "name": name, "active": active})

            if args.json:
                print(json.dumps(rows, indent=2))
            else:
                print(f"{'ID':<10}  {'ACTIVE':<7}  NAME")
                print("-" * 60)
                for r in rows:
                    print(f"{str(r['id']):<10}  {str(r['active']):<7}  {r['name']}")

        elif args.command == "submit":
            payload: Dict[str, Any] = {
                "name": args.name,
                "repo_url": args.repo_url,
                "machine_id": args.machine_id,
                "schedule_mode": args.schedule_mode,
            }
            # Optionals
            if args.email:
                payload["email"] = args.email
            if args.filename:
                payload["filename"] = args.filename
            if args.branch:
                payload["branch"] = args.branch
            
            if args.variables:
                usage_scenario_variables = {}
                for var in args.variables:
                    if "=" in var:
                        key, value = var.split("=", 1)
                        usage_scenario_variables[key.strip()] = value.strip()
                if usage_scenario_variables:
                    payload["usage_scenario_variables"] = usage_scenario_variables

            resp = client.submit_software(payload)
            if args.json:
                print(json.dumps(resp or {"status": "accepted"}, indent=2))
            else:
                if resp is None:
                    print("Accepted (202). Your submission was queued.")
                else:
                    # Mirror UI success semantics
                    if isinstance(resp, dict) and resp.get("success") is True:
                        print("Success. Save successful. Check your mail in ~10–15 minutes (if email provided).")
                    else:
                        print("Response received:")
                        print(json.dumps(resp, indent=2))
    except APIEmptyResponse204 as e:
        print(str(e))
        sys.exit(0)
    except APIError as e:
        print(f"API error: {e}", file=sys.stderr)
        sys.exit(1)
    except requests.RequestException as e:
        print(f"HTTP error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
