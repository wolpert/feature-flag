# Feature Flag
A minor implementation of Fowler's [feature toggle](https://martinfowler.com/articles/feature-toggles.html) 
designed for enabling and dialing up a feature for a percentage of traffic in
a consistent way. Provides for the following:

* Shared configuration for feature flags across multiple services (etcd, jdbc, etc)
* Quick disablement of a feature
* Ability to dial up a percentage of traffic based on a common identifier.
* Dial up is consistent. (At x% dial up, the same identifier will consistent be enabled or disabled)

## Status
![Feature-Flag Build](https://github.com/wolpert/feature-flag/actions/workflows/gradle.yml/badge.svg)

## Installation

All libraries are available on maven central.

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.codeheadsystems:feature-flag:1.0.3'
    implementation 'com.codeheadsystems:feature-flag-etcd:1.0.3'
    implementation 'com.codeheadsystems:feature-flag-ddb:1.0.3'
    implementation 'com.codeheadsystems:feature-flag-sql:1.0.3'
    implementation 'com.codeheadsystems:feature-flag-metrics:1.0.3'
}
```

## Notes
* SQL support just started, should work but not completely tested.
* Metrics here use the base metrics library from CodeHead. I may optimize them more.
* The use of the builder is now required. FeatureManager.Builder class should be used.

## Why not use an existing library?

* Current libraries are overly complicated or spring-based. (ff4j)
* Too many bells ans whistles and leaky abstractions. 

## Java Version Notice

These libraries are compiled with Java 21 but supports class files for Java 11.
However, DynamoDB requires Java 17 for testing due to using DynamoDBLocal
jars internally.

## TODO
1. Generic CLI would be nice.
