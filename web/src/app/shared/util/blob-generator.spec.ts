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

import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import * as fileSaver from 'file-saver-es';
import { BlobGenerator } from './blob-generator';

describe('BlobGenerator', () => {
  let saveAsSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    saveAsSpy = vi.spyOn(fileSaver, 'saveAs').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should call saveAs with a Blob and the given filename', () => {
    const obj = { key: 'value' };
    const fileName = 'test-output.json';

    BlobGenerator.saveFile(obj, fileName);

    expect(saveAsSpy).toHaveBeenCalledOnce();
    const [blobArg, nameArg] = saveAsSpy.mock.calls[0];
    expect(blobArg).toBeInstanceOf(Blob);
    expect(nameArg).toBe(fileName);
  });

  it('should create the Blob with application/json charset', async () => {
    const obj = { hello: 'world' };
    const fileName = 'output.json';

    BlobGenerator.saveFile(obj, fileName);

    const [blobArg] = saveAsSpy.mock.calls[0];
    // The browser normalises the MIME charset to lowercase
    expect((blobArg as Blob).type.toLowerCase()).toBe('application/json;charset=utf-8');
  });

  it('should serialize the object as pretty-printed JSON inside the Blob', async () => {
    const obj = { foo: 'bar', count: 42 };
    const fileName = 'data.json';

    BlobGenerator.saveFile(obj, fileName);

    const [blobArg] = saveAsSpy.mock.calls[0];
    const text = await (blobArg as Blob).text();
    expect(JSON.parse(text)).toEqual(obj);
  });

  it('should handle an empty object', () => {
    BlobGenerator.saveFile({}, 'empty.json');

    expect(saveAsSpy).toHaveBeenCalledOnce();
    const [blobArg, nameArg] = saveAsSpy.mock.calls[0];
    expect(blobArg).toBeInstanceOf(Blob);
    expect(nameArg).toBe('empty.json');
  });

  it('should handle an array as the object argument', () => {
    const arr = [1, 2, 3];
    BlobGenerator.saveFile(arr, 'array.json');

    expect(saveAsSpy).toHaveBeenCalledOnce();
  });
});
