# SpigotApi
## Description
SpigotApi allows you to do some fancy things not fully supported by the standard Bukkit/Spigot API. SpigotApi was firstly developped for older Minecraft versions like 1.2 or 1.15. During these versions you couldnt use NMS as "easily" as today because of the package name changing every versions. Right now (after 1.17 and newer versions) you can access those package without reflection and use them as you wish, but you need to understand how the Minecraft server is working, from obfuscated enums to packets. SpigotApi does all the dirty work for you, and you can use some cool methods inside your plugins!

## Version
Currently the SpigotApi only supports the 1.20 Spigot/Paper version. This is because attributes names can change from version to version and you cannot use it with others Minecraft versions. I will add the futures versions of Spigot/Paper when they appear, but I didnt plan to release SpigotApi for older versions.

## Features
### TeamAPI
This API registers your teams on the server side and send them when a player connects to the server. With this implementation you can; \
- Sort yours teams in the tab
- Add a prefix
- Add a suffix
- Set the team color
- Set the nametag visibility
- Disable the collisions
- Toggle friendly fire and see invisible friends properties

Here are some examples of uses;
```
boolean isRegistered = SpigotApi.getTeamApi().registerTeam(new TeamAPI.TeamBuilder("admin_team")
                .setPrefix("§cAdmin | ")
                .setColor(EnumChatFormat.a)
                .setSortOrder(10)
                .setFriendlyFire(false)
                .create())
if(isRegistered){
    System.out.println("New team registered!");
} else {
    System.err.println("The api failed to register your team");
}
```

## Spigot obfuscation 1.20
### EnumChatFormat
value |      color      | color code
- a   | "BLACK"         | '0'
- b   | "DARK_BLUE"     | '1'
- c   | "DARK_GREEN"    | '2'
- d   | "DARK_AQUA"     | '3'
- e   | "DARK_RED"      | '4'
- f   | "DARK_PURPLE"   | '5'
- g   | "GOLD"          | '6'
- h   | "GRAY"          | '7'
- i   | "DARK_GRAY"     | '8'
- j   | "BLUE"          | '9'
- k   | "GREEN"         | 'a'
- l   | "AQUA"          | 'b'
- m   | "RED"           | 'c'
- n   | "LIGHT_PURPLE"  | 'd'
- o   | "YELLOW"        | 'e'
- p   | "WHITE"         | 'f'
- q   | "OBFUSCATED"    | 'k'
- r   | "BOLD"          | 'l'
- s   | "STRIKETHROUGH" | 'm'
- t   | "UNDERLINE"     | 'n'
- u   | "ITALIC"        | 'o'
- v   | "RESET"         | 'r'
