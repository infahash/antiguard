

# antiguard
Fix class and package conflicts for android reverse engineer smali projects.

### About
If you are working on a smali project, this tool will help you to fix class-package conflicts.
think of a derectory structure like below.

<p align="center">
  <img src="https://github.com/infahash/antiguard/blob/master/asset/directory%20structure.PNG" />
</p>

in this example if you need to addess class `a` in package a/a/a/a, you can do it without errors.

But what happen if you need to access class `d` in package a/a/a/a/a. It will show error on java code because `package a` in a/a/a/a package conflicts with `class a` in a/a/a/a. 

you can avoid errors like above with **antiguard** tool.

It will automatically detect conflicts and rename package. finally it will fix path mapping on smali files according to renamed package.

------------



### Download & install

Latest version: v1.0 &nbsp;&nbsp;&nbsp; Update: 2018-06-06

<a href="https://github.com/infahash/antiguard/releases/download/v1.0/antiguard-1.0.jar" style="border: 10px 10px solid green">
  DOWNLOAD [antiguard-v1.0]
</a>

------------

### Usage
```
java -jar antiguard.jar path/to/smali/folder
```
* (replace *antiguard.jar* with your downloaded jar file name. eg:antiguard-1.0.jar)
* or rename your downloaded jar to *antiguard.jar*
