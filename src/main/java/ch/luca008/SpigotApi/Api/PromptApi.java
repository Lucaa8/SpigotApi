package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.SpigotApi;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PromptApi implements Listener {

    public interface PromptCallback{
        void getInput(boolean isCancelled, String[] asMultipleLines, String asSingleLine);
    }

    public String cancelCmd = "exit";
    public DyeColor promptColor = DyeColor.WHITE;

    private final Map<UUID, MainApi.PlayerSniffer> prompt = new HashMap<>();

    /**
     * This method opens a sign to the given player, puts the initial lines given on it, then waits that the player edit it and close it. The given callback will be called with the four lines the player wrote. <p>
     * The sign is placed at the player's location even though a block is already placed here. The original block will be recovered after the sign edition. <p>
     * The player can cancel all changes by typing the cancel cmd specified inside this api on the first line of the sign.
     * @param player The player you want to open the sign
     * @param callback The code you want to be executed when the player hits Done or closes the sign. The cancel argument is set to true when the first line on the sign is equals (ignore case) to the {@link #cancelCmd} attribute.
     * @param initialLines An array (size 4 max) with the lines you want to show to the player when the sign opens. You can set a blank line with an empty string. If the size is less than 4 then the array will be filled with blank lines.
     * @return Returns true if the player has been prompted with your lines and your callback. If the player is already in an old prompt then returns false.
     */
    public boolean promptPlayer(Player player, PromptCallback callback, String...initialLines) {

        final UUID id = player.getUniqueId();

        if(prompt.containsKey(id)){
            return false;
        }

        MainApi.PlayerSniffer sniffer = SpigotApi.getMainApi().players().handlePacket(player, ((packet, cancellable) -> {
            if(packet instanceof PacketPlayInUpdateSign sign){

                //The server wont read this packet and it'll be discarded
                cancellable.setCancelled(true);

                //We get the position of the fake sign to update this location and get back the original block before the sign place
                final BlockPosition blockpos = sign.a();
                Bukkit.getScheduler().runTask(SpigotApi.getInstance(), ()->new Location(player.getWorld(), blockpos.u(), blockpos.v(), blockpos.w()).getBlock().getState().update());

                //We build a single line from the 4 lines of the sign (may be useful if the player needs to write a sentence)
                String temp = "";
                for(String s : sign.d())temp+=s;
                final String line = temp;

                //We notify the caller that the player did finish to edit the sign
                callback.getInput(sign.d()[0].equalsIgnoreCase(cancelCmd), sign.d(), line);

                //We remove the player from the currently prompted players list as we have finished
                if(prompt.containsKey(id)){
                    prompt.remove(id).unregister();
                }

            }
        }));

        prompt.put(id, sniffer);

        prompt(player, initialLines);

        return true;

    }

    private void prompt(Player player, String...initialLines){

        if(initialLines==null||initialLines.length>4){
            initialLines = new String[]{"","","",""};
        }
        if(initialLines.length<4){
            String[] temp = new String[4];
            for(int i=0;i<4;i++){
                if(i<initialLines.length){
                    temp[i] = initialLines[i];
                }else{
                    temp[i] = "";
                }
            }
            initialLines = new String[4];
            System.arraycopy(temp, 0, initialLines, 0, 4);
        }

        Location l = player.getLocation();

        player.sendBlockChange(l, Material.OAK_SIGN.createBlockData());
        player.sendSignChange(l, initialLines, promptColor);

        SpigotApi.getMainApi().players().sendPacket(player, new PacketPlayOutOpenSignEditor(new BlockPosition(l.getBlockX(), l.getBlockY(), l.getBlockZ()), true));

    }

    @EventHandler
    public void OnPlayerQuit(PlayerQuitEvent e){
        prompt.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void OnPluginUnload(PluginDisableEvent e){
        if(e.getPlugin().equals(SpigotApi.getInstance())){
            for(Player p : Bukkit.getOnlinePlayers()){
                if(prompt.containsKey(p.getUniqueId())){
                    prompt.remove(p.getUniqueId()).unregister();
                }
            }
            prompt.clear();
        }
    }

}
