
rootProject.name = "minecraft_map2"
include("mcdsl")
include("mcdsl:airlock")
findProject(":mcdsl:airlock")?.name = "airlock"
include("airlock")
