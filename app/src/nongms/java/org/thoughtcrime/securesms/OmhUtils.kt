package org.thoughtcrime.securesms

import com.omh.android.maps.api.factories.OmhMapProvider

object OmhUtils {
  @JvmStatic
  fun initializeOmhMaps() {
    OmhMapProvider.Initiator()
      .addNonGmsPath()
      .initialize()
  }
}
