# PictureCompress
Simple tools to compress all png or jpeg files in you folder（Lossless compression）.


###大致描述
* 对于PNG图片
实际上是间接调用了`truepmg.exe`, `pngout.exe`, `cryopng.exe`, `pngwolf.exe`, `deflopt.exe`这五个程序进行压缩，实际压缩效果特别理想（实验测试一个共有9张png图片，共2,350,172字节的文件夹，用这个工具跑过之后大小减为2,061,774字节）。
目前这个工具只能在windows上面跑，原因是上面说的那五个工具我只在windows上面找全:(。
* 对于JPEG图片
实际上是调用`jpegtran.exe`。

###用法----------------------------------->
                     windows
----------------------------------->
首先你的电脑上需要有jre环境
* 将这个项目fork下来，直接运行`picture_compress.jar`
![](https://github.com/fenjuly/Mini/raw/master/mypiclib/picture_compress.png)

* Picture Path那里填上你需要处理的文件目录路径，Tool Path那里填上本项目在你电脑上的路径（注意：/bin /lib 这两个文件夹需要和picture_compress.jar放在同一级目录下）。最上面的空格是填你这个工具跑的时候所开的线程数，默认是10。

* 选择好了之就点击`confirm`开始运行吧 ～


###注意
* 点击`confirm`运行之后，这个工具首先做的操作是备份你需要处理的目录下面的所有图片。这里比较细节的一点是，它不是将所有的图片简单的备份到一个目录下面，而是将你的图片的位置信息也一起备份。举个例子：若你有一张图片路径为`myfolder/folder1/test.png`，那么这个工具备份后就是这样的：myfolderBackup/folder/test.png。

* 备份完后就开始压缩，你可能担心的地方有：

  1. 若某些图片用这个工具处理后，大小不仅没变小反而变大了怎么办？ 

  2. 若对于某些图片，在处理到一半的时候出错了怎么办？
  
对于第一点，若发现处理过后大小反而变大，那么这个工具就会有先前备份的图片替换处理过的图片。第二点，同理，若发现出错，同样会从备份文件里面恢复。

* 完成压缩后，你可以点击`open log file`（文件处于myfolderBackup/下）来查看每一个图片的压缩信息。
![](https://raw.githubusercontent.com/fenjuly/Mini/master/mypiclib/compress_log.png)

* 另外就是，如果这个工具处理某些图片的过程中出错了（可能这是一个jpg格式的图片但是却是png的文件拓展:( ）那么这个工具会在处理完后生成一个白名单文件，工具会将这个出错的图片加入白名单。又或者某些图片你根本不想处理，你可以手动添加一个白名单。这个白名单位于folderBackup/whiteName.txt

代码很烂，建议不要看。。。
欢迎大家使用和吐槽:)。

###用法----------------------------------->
                     linux or mac
----------------------------------->

1、先安装wine，详细可参考https://www.winehq.org/download
2、输入命令： source ScriptPNG.sh png目录
   脚本会自动在png目录的同一级目录备份一份png资源