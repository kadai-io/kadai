/*
 * Copyright [2026] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { BlobGenerator } from './blob-generator';

describe('BlobGenerator', () => {
  let createUrlSpy: ReturnType<typeof vi.spyOn>;
  let clickSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    createUrlSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:test');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {});
    clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
  });

  const savedBlob = () => createUrlSpy.mock.calls[0][0] as Blob;
  const clickedLink = () => clickSpy.mock.contexts[0] as HTMLAnchorElement;

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should click a download link for the Blob with the given filename', () => {
    BlobGenerator.saveFile({ key: 'value' }, 'test-output.json');

    expect(clickSpy).toHaveBeenCalledOnce();
    expect(savedBlob()).toBeInstanceOf(Blob);
    expect(clickedLink().href).toBe('blob:test');
    expect(clickedLink().download).toBe('test-output.json');
  });

  it('should revoke the object URL after the click', async () => {
    BlobGenerator.saveFile({}, 'x.json');

    await new Promise((resolve) => setTimeout(resolve));
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:test');
  });

  it('should create the Blob with application/json charset', async () => {
    const obj = { hello: 'world' };
    const fileName = 'output.json';

    BlobGenerator.saveFile(obj, fileName);

    const blobArg = savedBlob();
    // The browser normalises the MIME charset to lowercase
    expect((blobArg as Blob).type.toLowerCase()).toBe('application/json;charset=utf-8');
  });

  it('should serialize the object as pretty-printed JSON inside the Blob', async () => {
    const obj = { foo: 'bar', count: 42 };
    const fileName = 'data.json';

    BlobGenerator.saveFile(obj, fileName);

    const blobArg = savedBlob();
    const text = await (blobArg as Blob).text();
    expect(JSON.parse(text)).toEqual(obj);
  });

  it('should handle an empty object', () => {
    BlobGenerator.saveFile({}, 'empty.json');

    expect(clickSpy).toHaveBeenCalledOnce();
    expect(savedBlob()).toBeInstanceOf(Blob);
    expect(clickedLink().download).toBe('empty.json');
  });

  it('should handle an array as the object argument', () => {
    const arr = [1, 2, 3];
    BlobGenerator.saveFile(arr, 'array.json');

    expect(clickSpy).toHaveBeenCalledOnce();
  });
});
