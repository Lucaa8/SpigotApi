package ch.luca008.SpigotApi.Api;

import ch.luca008.SpigotApi.Packets.SignPackets;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PromptApi {

    public interface PromptCallback{
        void getInput(boolean isCancelled, String[] asMultipleLines, String asSingleLine);
    }

    private final List<UUID> currentPrompts = new ArrayList<>();

    /**
     * This method opens a sign to the given player, puts the initial lines given on it, then waits that the player edit it and close it. The given callback will be called with the four lines the player wrote. <p>
     * The sign is placed at the player's location even though a block is already placed here. The original block will be recovered after the sign edition. <p>
     * The player can cancel all changes by typing the cancel cmd specified inside this api on the first line of the sign.
     * @param player The player you want to open the sign
     * @param callback The code you want to be executed when the player hits Done or closes the sign. The cancel argument is set to true when the first line on the sign is equals (ignore case) to the <b>exitCmd</b> argument.
     * @param linesColor The color the lines will be displayed on the sign (default when null: black)
     * @param exitCmd A string which will cancel the prompt (second param of callback will be false) if the player writes it on the first sign's line. (can be null if you do not need a cancellable)
     * @param initialLines An array (size 4 max) with the lines you want to show to the player when the sign opens. You can set a blank line with an empty string. If the size is less than 4 then the array will be filled with blank lines.
     * @return Returns true if the player has been prompted with your lines and your callback. If the player is already in an old prompt then returns false.
     */
    public boolean promptPlayer(Player player, PromptCallback callback, @Nullable DyeColor linesColor, @Nullable String exitCmd, String...initialLines) {

        final UUID id = player.getUniqueId();

        if(currentPrompts.contains(id)){
            return false;
        }

        SpigotApi.getMainApi().players().startHandling(player, "SpigotApi_Prompt", ((packet, cancellable) -> {

            if(SignPackets.isUpdateSignPacket(packet)){

                //The server won't read this packet and it'll be discarded
                cancellable.setCancelled(true);

                //We get the position and the lines of the fake sign
                Location blockPos = new Location(null, 0.0, 0.0, 0.0);
                String[] lines = new String[4];
                SignPackets.getPacketInInfo(packet, blockPos, lines);

                //We update this location and get back the original block before the sign place
                Bukkit.getScheduler().runTask(SpigotApi.getInstance(), ()->new Location(player.getWorld(), blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ()).getBlock().getState().update());

                //We build a single line from the 4 lines of the sign (may be useful if the player needs to write a sentence)
                String temp = "";
                for(String s : lines)temp+=s;
                final String line = temp;

                //We notify the caller that the player did finish to edit the sign
                //Because the packets are received asynchronously we need to resync with bukkit to avoid async errors inside the callback.
                Bukkit.getScheduler().runTask(SpigotApi.getInstance(), ()->callback.getInput(lines[0].equalsIgnoreCase(exitCmd), lines, line));

                //We remove the player from the currently prompted players list as we have finished
                currentPrompts.remove(id);
                SpigotApi.getMainApi().players().stopHandling(player, "SpigotApi_Prompt");

            }
        }));

        prompt(player, linesColor==null?DyeColor.BLACK:linesColor, initialLines);

        return true;

    }

    private void prompt(Player player, DyeColor color, String...initialLines){

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
        player.sendSignChange(l, initialLines, color); //color does not seem to work on 1.20.2 only, text is brown even if I put white. 1.20.1 and 1.20.4 look like ok, weird.

        SignPackets.openSign(l).send(player);

    }

}
