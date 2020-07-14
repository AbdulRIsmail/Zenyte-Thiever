package Script;

import com.sun.xml.internal.ws.api.ResourceLoader;
import net.runelite.api.ChatMessageType;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.script.Script;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@ScriptManifest(author = "Sultan", category = Category.THIEVING, description = "Master Thief", discord = "", name = "Master Thiever", servers = { "Zenyte" }, version = "1")

public class Thiever extends Script implements MouseListener {

    private long timeBegan;  // how long script was running
    private boolean showPaint = false; // show pain or not
    private int successfulPickpocket = 0;
    private boolean stunned = false;
    private float moneyMade = 0;
    private String status = "Script Started";

    // discord bot
    public static boolean sendMessages = true;
    public static boolean pauseScript = false;
    public static String lastSenderName = "";

    public void onExecute() {
        try { new Bot(); } catch (LoginException ignored) { }
        ctx.updateStatus("Started AIO Thiever");
        timeBegan = System.currentTimeMillis();
        showPaint = true;
    }

    @Override
    public void onProcess() {
        if (!pauseScript) {
            SimpleNpc npc = ctx.npcs.populate().filter(3104, 3010, 3079, 3085).nearest().next();

            if (npc != null && npc.validateInteractable() && !stunned) {
                status = "Pickpocket";
                npc.click("Pickpocket");
                ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() != -1, 300);
            }

            if(!ctx.inventory.populate().filter(22529, 22521, 22526).isEmpty()) {
                if(ctx.inventory.populate().filter(22529, 22521, 22526).next().getQuantity() > 10) {
                    status = "Opening coin pouch";
                    ctx.sleep(1000);
                    ctx.inventory.populate().filter(22529, 22521, 22526).next().click(0);
                }
            }

            if (!ctx.inventory.populate().filter("Sardine", "Trout", "Salmon", "Tuna", "Lobster", "Swordfish", "Monkfish", "Shark").isEmpty()) {
                if(ctx.players.getLocal().getHealth() < 15) {
                    status = "Eating food";
                    ctx.sleep(1000);
                    ctx.inventory.populate().filter("Sardine", "Trout", "Salmon", "Tuna", "Lobster", "Swordfish", "Monkfish", "Shark").next().click(0);
                }
            } else {
                ctx.sleep(2000);
                ctx.sendLogout();
            }

            if (stunned) {
                ctx.sleep(3000);
                stunned = false;
            }
        }
    }

    @Override
    public void onTerminate() {
        Bot.jda.shutdownNow();
    }

    @Override
    public void onChatMessage(ChatMessage c) {
        if (c.getType() == ChatMessageType.SPAM) {
            String msg = c.getMessage().toLowerCase();
            if (msg.contains("you successfully pick the")) successfulPickpocket++;
            if (msg.contains("you have been stunned")) {
                stunned = true;
                status = "Stunned";
            }
        }

        if (e.getType() == ChatMessageType.PUBLICCHAT && sendMessages) {
            String message = e.getMessage();
            String sender = e.getName();
            if (!sender.equalsIgnoreCase("extreme")) {
                // regex to replace players with donor/mod signs beside the name
                sender = sender.replaceAll("<[^>]*>", "🔹️");

                if (lastSenderName.equals(sender)) {
                    new WebHook(sender, message, true);
                } else {
                    lastSenderName = sender;
                    new WebHook(sender, message, false);
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setFont(new Font("Ubuntu", Font.PLAIN, 12));

        if (!showPaint) {
            g.setColor(new Color(30, 30, 30));
            g.fillRect(295, 416, 100, 25);
            g.setColor(new Color(186, 0, 186));
            g.drawString("Show Paint", 305, 433); // shows how long the bot has been running for
        }

        if (showPaint) {
            long timeRan = System.currentTimeMillis() - this.timeBegan;
            g.drawImage(getImage(), 7, 345, null);
            g.setColor(new Color(186, 0, 186));
            g.drawString("Running: " + ft(timeRan), 55, 402); // shows how long the bot has been running for
            g.drawString("Status: " + status, 55, 433); // shows how long the bot has been running for
            g.drawString("Hide Paint", 305, 433); // shows how long the bot has been running for
            g.drawString("Successful: " + successfulPickpocket, 55, 463); // shows how long the bot has been running for
            g.drawString("3002k (200K)", 305, 402); // shows how long the bot has been running for
        }
    }

    private BufferedImage getImage() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(this.getClass().getResource("/Data/thiever.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    private String ft(long duration) {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60) % 60);
        long hours = (duration / (1000 * 60 * 60) % 24);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void sendMessage(String message) {
        ClientContext.getAPI().keyboard.sendKeys(message);
    }

    public static void getPlayers() {
        SimplePlayerQuery<SimplePlayer> players = ClientContext.getAPI().players.populate();
        if (players.size() == 0) {
            new WebHook("No players nearby!", false);
        } else {
            for (SimplePlayer player: players) {
                new WebHook("", player.getName(), false);
            }
        }
    }

    public static void getStaffs() {
        SimplePlayerQuery<SimplePlayer> staffs = ClientContext.getAPI().antiBan.nearbyStaff();
        if (staffs.size() == 0) {
            new WebHook("No staff nearby!", false);
        } else {
            for (SimplePlayer staff: staffs) {
                new WebHook("", staff.getName(), false);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        Rectangle togglePaint = new Rectangle(295, 416, 100, 25);
        if (togglePaint.contains(p)) showPaint = !showPaint;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
