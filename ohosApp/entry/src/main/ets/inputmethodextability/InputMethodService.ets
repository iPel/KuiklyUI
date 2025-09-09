import { hilog } from '@kit.PerformanceAnalysisKit';
import { InputMethodExtensionAbility } from '@kit.IMEKit';
import { Want } from '@kit.AbilityKit';
import keyboardController from './model/KeyboardController';

export default class InputMethodExtAbility extends InputMethodExtensionAbility {
  onCreate(want: Want) {
    hilog.info(0x0000, 'testTag', '%{public}s', 'InputDemoService onCreate');
    keyboardController.onCreate(this.context);
  }

  onDestroy() {
    hilog.info(0x0000, 'testTag', '%{public}s', 'onDestroy.');
    keyboardController.onDestroy();
  }
}