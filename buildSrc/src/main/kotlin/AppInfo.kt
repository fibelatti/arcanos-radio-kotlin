object AppInfo {
    const val appName = "Arcanos Radio"
    const val applicationId = "de.developercity.arcanosradio"

    private const val versionMajor = 2
    private const val versionMinor = 0
    private const val versionPatch = 0

    val versionCode: Int = versionMajor * 10000 + versionMinor * 100 + versionPatch

    val versionName: String = "${AppInfo.versionMajor}.${AppInfo.versionMinor}.${AppInfo.versionPatch}"
}
