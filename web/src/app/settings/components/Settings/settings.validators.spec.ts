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

import { describe, expect, it } from 'vitest';
import { validateSettings } from './settings.validators';
import { Settings, SettingTypes } from '../../models/settings';

describe('validateSettings', () => {
  it('should return no invalid members for a Text type within bounds (min and max)', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Label', key: 'myText', type: SettingTypes.Text, min: 2, max: 10 }]
        }
      ],
      myText: 'hello'
    };
    const result = validateSettings(settings);
    expect(result).toEqual([]);
  });

  it('should mark a Text type as invalid when value length is below min', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Label', key: 'myText', type: SettingTypes.Text, min: 5, max: 20 }]
        }
      ],
      myText: 'hi'
    };
    const result = validateSettings(settings);
    expect(result).toContain('myText');
  });

  it('should mark a Text type as invalid when value length exceeds max', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Label', key: 'myText', type: SettingTypes.Text, min: 1, max: 3 }]
        }
      ],
      myText: 'toolong'
    };
    const result = validateSettings(settings);
    expect(result).toContain('myText');
  });

  it('should mark a Text type as invalid when value length is below min-only constraint', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Label', key: 'myText', type: SettingTypes.Text, min: 5 }]
        }
      ],
      myText: 'ab'
    };
    const result = validateSettings(settings);
    expect(result).toContain('myText');
  });

  it('should mark a Text type as invalid when value length exceeds max-only constraint', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Label', key: 'myText', type: SettingTypes.Text, max: 3 }]
        }
      ],
      myText: 'waytoolong'
    };
    const result = validateSettings(settings);
    expect(result).toContain('myText');
  });

  it('should return no invalid members for a Text type with no min/max constraints', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Label', key: 'myText', type: SettingTypes.Text }]
        }
      ],
      myText: 'anything goes'
    };
    const result = validateSettings(settings);
    expect(result).toEqual([]);
  });

  it('should return no invalid members for a valid Interval type within bounds', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Range', key: 'myInterval', type: SettingTypes.Interval, min: 0, max: 100 }]
        }
      ],
      myInterval: [10, 80]
    };
    const result = validateSettings(settings);
    expect(result).toEqual([]);
  });

  it('should mark an Interval type as invalid when lower bound is below min', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Range', key: 'myInterval', type: SettingTypes.Interval, min: 5, max: 100 }]
        }
      ],
      myInterval: [2, 80]
    };
    const result = validateSettings(settings);
    expect(result).toContain('myInterval');
  });

  it('should mark an Interval type as invalid when upper bound exceeds max', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Range', key: 'myInterval', type: SettingTypes.Interval, min: 0, max: 50 }]
        }
      ],
      myInterval: [10, 80]
    };
    const result = validateSettings(settings);
    expect(result).toContain('myInterval');
  });

  it('should mark an Interval type as invalid when value[0] > value[1]', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Range', key: 'myInterval', type: SettingTypes.Interval }]
        }
      ],
      myInterval: [90, 10]
    };
    const result = validateSettings(settings);
    expect(result).toContain('myInterval');
  });

  it('should return no invalid members for a valid JSON string', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Config', key: 'myJson', type: SettingTypes.Json }]
        }
      ],
      myJson: '{"key": "value"}'
    };
    const result = validateSettings(settings);
    expect(result).toEqual([]);
  });

  it('should mark a Json type as invalid for a malformed JSON string', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Config', key: 'myJson', type: SettingTypes.Json }]
        }
      ],
      myJson: '{not valid json'
    };
    const result = validateSettings(settings);
    expect(result).toContain('myJson');
  });

  it('should return no invalid members when schema has no members with constraints', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Color', key: 'myColor', type: SettingTypes.Color }]
        }
      ],
      myColor: '#ff0000'
    };
    const result = validateSettings(settings);
    expect(result).toEqual([]);
  });

  it('should handle multiple groups and multiple members', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group A',
          members: [
            { displayName: 'Text A', key: 'textA', type: SettingTypes.Text, min: 1, max: 5 },
            { displayName: 'Json A', key: 'jsonA', type: SettingTypes.Json }
          ]
        },
        {
          displayName: 'Group B',
          members: [{ displayName: 'Text B', key: 'textB', type: SettingTypes.Text, min: 1, max: 5 }]
        }
      ],
      textA: 'ok',
      jsonA: '{"valid": true}',
      textB: 'this string is too long for the max constraint'
    };
    const result = validateSettings(settings);
    expect(result).not.toContain('textA');
    expect(result).not.toContain('jsonA');
    expect(result).toContain('textB');
  });

  it('should accept min of 0 as a valid minimum constraint', () => {
    const settings: Settings = {
      schema: [
        {
          displayName: 'Group',
          members: [{ displayName: 'Text', key: 'myText', type: SettingTypes.Text, min: 0, max: 10 }]
        }
      ],
      myText: ''
    };
    const result = validateSettings(settings);
    expect(result).toEqual([]);
  });
});
