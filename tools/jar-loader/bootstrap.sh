#!/bin/bash

shopt -s globstar

VERSION='3.4.0'

mkdir -p src/main/java/extrarulesjava/jarloader/

curl -L https://github.com/spring-projects/spring-boot/archive/v${VERSION}.tar.gz -o spring-boot.tar.gz

tar -x -f spring-boot.tar.gz \
       -C src/main/java/extrarulesjava/jarloader/ \
       spring-boot-${VERSION}/spring-boot-project/spring-boot-tools/spring-boot-loader/src/main/java/org/springframework/boot/loader/ \
       --strip-components=11

rm -rf src/**/{jarmode,nio}/ \
       src/**/{JarLauncher,JarModeRunner,PropertiesLauncher,SystemPropertyUtils,WarLauncher}.java \
       src/**/package-info.java

sed -i 's|org.springframework.boot.loader|extrarulesjava.jarloader|g' src/**/*.java

rm spring-boot.tar.gz
