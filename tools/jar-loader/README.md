A jar loader that supports nested jars, based on Spring Boot's [executable jar format](https://docs.spring.io/spring-boot/3.5/specification/executable-jar/).

Compatible jars should be structured as follows:

```
├─ META-INF/
│  └─ MANIFEST.MF
│
├─ extrarulesjava/
│  └─ jarloader/
│     └─ <jar loader classes>
│
└─ jars/
   ├─ <application jars>
   └─ <dependency jars>
```

The `extrarulesjava.jarloader.JarLoader` class serves as the main entry point.
The actual main class should be specified in the `Start-Class` attribute:

```
Main-Class: extrarulesjava.jarloader.JarLoader
Start-Class: <main class>
```
