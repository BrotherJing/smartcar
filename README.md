# 工科创3C进度报告

## 2015.10.14

加入了语音识别和增强现实模块

- 语音识别使用百度语音识别SDK
- 增强现实使用Vuforia

## 2015.10.13

实现了server到client的静态图片的发送

- 在数据传输中加入了简单的protocol，可以发送文字和图片
- 在client加入了LRUCache，用于在内存中管理图片

## 2015.10.11

重构了server端和client端的代码：

- 通信部分放在service中，通过binder和broadcast与activity交互

## 2015.9.27

基本实现传感器部分的代码，用于双机相对方位确定

## 2015.9.25

基本完成了双机/多机TCP通信模块：

- server端可接收多个client的请求并反馈
- client端可发送请求，并接受server的反馈

## 2015.9.20

initial commit