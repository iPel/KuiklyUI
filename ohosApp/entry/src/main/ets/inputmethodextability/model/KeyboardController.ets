import { common } from '@kit.AbilityKit';
import { display, window } from '@kit.ArkUI';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { inputMethodEngine } from '@kit.IMEKit';
import { BusinessError } from '@ohos.base';

const inputMethodAbility: inputMethodEngine.InputMethodAbility = inputMethodEngine.getInputMethodAbility();
const WINDOW_NAME: string = 'inputApp';
const WINDOW_TYPE_INPUT_METHOD: number = 2105;

class KeyboardController {
  private mContext: common.ExtensionContext | undefined = undefined;
  private textInputClient: inputMethodEngine.InputClient | undefined = undefined;
  private keyboardController: inputMethodEngine.KeyboardController | undefined = undefined;
  private nonBarPosition: number = 0;
  private panel: inputMethodEngine.Panel | undefined = undefined;
  private floatPanel: inputMethodEngine.Panel | undefined = undefined;

  constructor() {
  }

  public onCreate(context: common.ExtensionContext): void {
    this.mContext = context;
    this.initWindow();
    this.registerListener();
  }

  public insertText(text: string): void {
    if (this.textInputClient) {
      this.textInputClient.insertText(text);
    }
  }

  public deleteForward(length: number): void {
    if (this.textInputClient) {
      this.textInputClient.deleteForward(length);
    }
  }

  public onDestroy(): void {
    this.unRegisterListener();
    let win = window.findWindow(WINDOW_NAME);
    win.destroyWindow();
  }

  private initWindow(): void {
    if (this.mContext === undefined) {
      return;
    }
    let dis = display.getDefaultDisplaySync();
    let dWidth = dis.width;
    let dHeight = dis.height;
    let keyHeightRate = 0.47;
    let keyHeight = dHeight * keyHeightRate;
    this.nonBarPosition = dHeight - keyHeight;

    const panelInfo: inputMethodEngine.PanelInfo = {
      type: inputMethodEngine.PanelType.SOFT_KEYBOARD,
      flag: inputMethodEngine.PanelFlag.FLG_FIXED
    };
    inputMethodAbility.createPanel(this.mContext, panelInfo).then(async (inputPanel: inputMethodEngine.Panel) => {
      this.panel = inputPanel;
      if(this.panel) {
        // 设置 panel 大小
        await this.panel.resize(dWidth, keyHeight);
        // 设置展示键盘 UI
        await this.panel.setUiContent('inputmethodextability/pages/Index');
      }
    });

    const floatPanelInfo: inputMethodEngine.PanelInfo = {
      type: inputMethodEngine.PanelType.STATUS_BAR,
      flag: inputMethodEngine.PanelFlag.FLG_FLOATING
    };
    inputMethodAbility.createPanel(this.mContext, floatPanelInfo).then(async (inputPanel: inputMethodEngine.Panel) => {
      this.floatPanel = inputPanel
      if (this.floatPanel) {
        this.floatPanel.setUiContent("inputmethodextability/pages/FloatPage")
      }
    }).catch((err: BusinessError)=> {
      console.log("create float panel failed, error: " + JSON.stringify(err))
    });
  }

  private registerListener(): void {
    this.registerInputListener();
    inputMethodAbility.on('keyboardShow', () => {
      // this.showHighWindow();
    });
    inputMethodAbility.on('keyboardHide', () => {
      if (this.mContext) {
        this.mContext.getApplicationContext().killAllProcesses();
      }
    });
  }

  private registerInputListener() {
    inputMethodAbility.on('inputStart',
      (kbController: inputMethodEngine.KeyboardController, textInputClient: inputMethodEngine.InputClient) => {
        this.textInputClient = textInputClient;
        this.keyboardController = kbController;
      })
    inputMethodAbility.on('inputStop', () => {
      this.onDestroy();
    });
  }

  private unRegisterListener(): void {
    inputMethodAbility.off('inputStart');
    inputMethodAbility.off('inputStop', () => {
    });
    inputMethodAbility.off('keyboardShow');
  }

  public showFloatPanel() {
    if (this.floatPanel) {
      this.floatPanel.resize(100, 100)
      this.floatPanel.moveTo(0, 0)
      this.floatPanel.show()
    }
  }

  public hideFloatPanel() {
    if (this.floatPanel) {
      this.floatPanel.hide()
    }
  }

}

const keyboardController = new KeyboardController();

export default keyboardController;