package org.neteinstein.family.analytics

/**
 * Value reported as the `platform` analytics user property, so any metric can be split by the
 * client it came from - the single most-asked question about this app being whether the Web build
 * gets used at all.
 *
 * An `expect val` rather than a parameter of [org.neteinstein.family.di.doInitKoin], because that
 * entry point is shared by iOS and Web and is deliberately zero-argument (Kotlin/Native's
 * Objective-C export drops Kotlin default parameter values, so iosApp's Swift would have to pass
 * one explicitly - see its kdoc).
 */
expect val analyticsPlatformName: String
