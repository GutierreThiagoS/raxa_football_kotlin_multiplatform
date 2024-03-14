@file:OptIn(org.jetbrains.compose.resources.InternalResourceApi::class,)

package res

import kotlin.OptIn
import org.jetbrains.compose.resources.ExperimentalResourceApi

@ExperimentalResourceApi
internal object ResMain {
  /**
   * Reads the content of the resource file at the specified path and returns it as a byte array.
   *
   * Example: `val bytes = Res.readBytes("files/key.bin")`
   *
   * @param path The path of the file to read in the compose resource's directory.
   * @return The content of the file as a byte array.
   */

  object drawable

  object assert

}
