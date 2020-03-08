# GenAllSetter Readme

### 简介

Java程序员最害怕的就是给对象赋值。当对象的字段特别多时，挨个调用赋值语句可能会造成遗漏，导致产生bug，而且还是一个没有技术含量的体力活。GenAllSetter目的在会帮助程序员减轻痛苦，并提高效率。

### 使用说明

在本工程中直接下载genAllSetter-1.1.zip到本地，然后打开 IDEA

- Preferences(Settings) > Plugins > install plugin from disk > find "genAllSetter-1.1.zip" > Install Plugin > restart

![gifhome_1920x1080_18s](/material/gifhome_1920x1080_18s.gif)

- Set模式

  在当前声明的构建对象上，召唤Generate菜单，然后选择生成带默认值参数的所有set方法或者不带参。

- Builder模式

  需要先声明builder方法，然后在builder语句上，召唤Generate菜单，则可与同上一样生成所有赋值语句，仅支持Lombok的@Builder注解。


