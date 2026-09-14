package org.neteinstein.family.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette – warm amber/gold family
val PrimaryLight = Color(0xFFBF6900)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFFDDB5)
val OnPrimaryContainerLight = Color(0xFF3D1E00)

val SecondaryLight = Color(0xFF6F5B40)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFF9DEBA)
val OnSecondaryContainerLight = Color(0xFF271904)

val TertiaryLight = Color(0xFF4D6544)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFCFEBC0)
val OnTertiaryContainerLight = Color(0xFF0B2008)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFFF8F4)
val OnBackgroundLight = Color(0xFF201A14)
val SurfaceLight = Color(0xFFFFF8F4)
val OnSurfaceLight = Color(0xFF201A14)
val SurfaceVariantLight = Color(0xFFF0E0CF)
val OnSurfaceVariantLight = Color(0xFF4F4539)
val OutlineLight = Color(0xFF817567)

// Surface container tones (menus, sheets, elevated cards) - not part of Material3's minimal
// lightColorScheme()/darkColorScheme() params, but left unset they fall back to the library's
// default purple baseline instead of this app's warm palette (e.g. DropdownMenu reads
// surfaceContainer). Stepped between BackgroundLight and SurfaceVariantLight.
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFFFF1E5)
val SurfaceContainerLight = Color(0xFFFCECDC)
val SurfaceContainerHighLight = Color(0xFFF7E4CE)
val SurfaceContainerHighestLight = Color(0xFFF1DCC0)

// Dark palette
val PrimaryDark = Color(0xFFFFB95A)
val OnPrimaryDark = Color(0xFF652E00)
val PrimaryContainerDark = Color(0xFF914200)
val OnPrimaryContainerDark = Color(0xFFFFDDB5)

val SecondaryDark = Color(0xFFDCC3A0)
val OnSecondaryDark = Color(0xFF3D2E16)
val SecondaryContainerDark = Color(0xFF55432A)
val OnSecondaryContainerDark = Color(0xFFF9DEBA)

val TertiaryDark = Color(0xFFB3CEA5)
val OnTertiaryDark = Color(0xFF203619)
val TertiaryContainerDark = Color(0xFF364D2E)
val OnTertiaryContainerDark = Color(0xFFCFEBC0)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF18120C)
val OnBackgroundDark = Color(0xFFEDE0D4)
val SurfaceDark = Color(0xFF18120C)
val OnSurfaceDark = Color(0xFFEDE0D4)
val SurfaceVariantDark = Color(0xFF4F4539)
val OnSurfaceVariantDark = Color(0xFFD3C3B2)
val OutlineDark = Color(0xFF9C8E7D)

// Surface container tones, dark counterpart of the light set above. Stepped between
// BackgroundDark and SurfaceVariantDark.
val SurfaceContainerLowestDark = Color(0xFF120D08)
val SurfaceContainerLowDark = Color(0xFF1D160F)
val SurfaceContainerDark = Color(0xFF221B13)
val SurfaceContainerHighDark = Color(0xFF2D2419)
val SurfaceContainerHighestDark = Color(0xFF383024)
