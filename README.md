# 工科创3C进度报告

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