import { numberSourceListData, sourceListType } from './keyboardKeyData'
import keyboardController from '../model/KeyboardController';
import { NodeContent } from '@ohos.arkui.node';
import { imeDemo } from 'libkuikly_entry.so';

@Component
struct keyItem {
  @State keyBgc: string = "#fff";
  @State keyFontColor: string = "#000";
  private keyValue: sourceListType = { content: '' };

  build() {
    Column() {
      Flex({
        direction: FlexDirection.Column,
        alignItems: ItemAlign.Center, justifyContent: FlexAlign.Center
      }) {
        Text(this.keyValue.content).fontSize(20).fontColor(this.keyFontColor)
      }
    }
    .backgroundColor(this.keyBgc)
    .borderRadius(6)
    .width("8%")
    .height("65%")
    .onClick(() => {
      keyboardController.insertText(this.keyValue.content);
    })
  }
}

@Component
export struct deleteItem {
  @State keyBgc: string = "#fff";
  @State keyFontColor: string = "#000";

  build() {
    Column() {
      Flex({
        direction: FlexDirection.Column,
        alignItems: ItemAlign.Center, justifyContent: FlexAlign.Center
      }) {
        Text("delete").fontSize(20).fontColor(this.keyFontColor)
      }
    }
    .backgroundColor(this.keyBgc)
    .width("20%")
    .borderRadius(6)
    .onClick(() => {
      keyboardController.deleteForward(1);
    })
  }
}

@Component
struct numberMenu {
  private numberList: sourceListType[] = [];

  build() {
    Flex({ direction: FlexDirection.Column, alignItems: ItemAlign.Center, justifyContent: FlexAlign.SpaceEvenly }) {
      Flex({ justifyContent: FlexAlign.SpaceBetween }) {
        ForEach(this.numberList, (item: sourceListType) => { // 数字键盘第一行
          keyItem({ keyValue: item })
        }, (item: sourceListType) => item.content);
      }
      .padding({ top: "2%" })
      .width("96%")
      .height("25%")

      Flex({ justifyContent: FlexAlign.SpaceBetween }) {
        deleteItem()
      }
      .width("96%")
      .height("25%")
    }
  }
}

@Entry
@Component
struct Index {
  private numberList: sourceListType[] = numberSourceListData;
  private nodeContent: Content = new NodeContent();

  aboutToAppear(): void {
    imeDemo(this.nodeContent);
  }

  build() {
    Stack() {
      Flex({
        direction: FlexDirection.Column,
        alignItems: ItemAlign.Center,
        justifyContent: FlexAlign.End
      }) {
        Flex({
          direction: FlexDirection.Column,
          alignItems: ItemAlign.Center,
          justifyContent: FlexAlign.SpaceBetween
        }) {
          numberMenu({ numberList: this.numberList })
        }
        .align(Alignment.End)
        .width("100%")
        .height(150)

        Row() {
          Button("show panel")
            .onClick(() => {
              keyboardController.showFloatPanel();
            })
          Button("hide panel")
            .onClick(() => {
              keyboardController.hideFloatPanel();
            })
        }

        Stack() {
          ContentSlot(this.nodeContent);
        }.size({
          width: 100, height: 100
        })
      }
      .height("100%")
      .align(Alignment.End)
      .backgroundColor("#cdd0d7")
    }
    .position({ x: 0, y: 0 })
    .zIndex(99999)
  }
}