package discordbot;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.managers.Presence;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.jpaste.exceptions.PasteException;
import org.jpaste.pastebin.PasteExpireDate;
import org.jpaste.pastebin.PastebinPaste;
import org.json.*;

/**
 *
 * @author armaa
 */
public class EventListenerAdapter extends ListenerAdapter {

    private Random _rnd = new Random();
    private String serverId;
    private String serverName;
    private DateTime uptime;
    private final static String[] SLOTS_ICONS = { ":cherries:", ":lemon:", ":grapes:", ":seven:", ":tangerine:", ":watermelon:", ":banana:", ":pear:", ":strawberry:" };
    private final static String[] SLOTS_MESSAGE = { "rolled the slots!", "is trying their luck!", "pulled the handle!" };
    private final static String PASTEBIN_API_KEY = "b30d5c6673c98fb0c581dd99de644e44";
    private final static String ADMIN_ID = "97361468952936448";

    EventListenerAdapter(DateTime uptime) {
        this.uptime = uptime;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) 
            return;
        
        JDA jda = event.getJDA();
        
        User user = event.getAuthor();
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        Member member = event.getMember();
        String msg = message.getContent();
        
        serverId = event.getGuild().getId();
        serverName = event.getGuild().getName();
        
        if (messageIsBanned(msg, channel))  {
            message.deleteMessage().queue();
            return;
        }
        
        if(msg.toLowerCase().contains("buck") || msg.contains("$")) {
            try {
                channel.sendFile(new File("files/buck.png"), null).queue();
            } catch (IOException e) {
                channel.sendMessage("Something went wrong..").queue();
            }
        }
        
        // Sends the user a private message with list of all commands
        if (msg.startsWith(".help")) {
            user.openPrivateChannel().queue(i -> i.getUser().getPrivateChannel().sendMessage(getListOfCommands()).queue());
        }
        
        else if (msg.startsWith(".ready")) {
            try {
                channel.sendFile(new File("files/READY.jpg"), null).queue();
            } catch (IOException e) {
                channel.sendMessage("Something went wrong..").queue();
            }
        }
        
        else if(msg.startsWith(".ping")) {
            String ping = "-1";
            
            try {
                String s;
                List<String> commands = new ArrayList<>();
                commands.add("ping");
                commands.add("127.0.0.1");
                ProcessBuilder processbuilder = new ProcessBuilder(commands);
                Process process = processbuilder.start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((s = stdInput.readLine()) != null) {
                    if (s.contains("Average"))
                        ping = s.substring(s.lastIndexOf(" "));
                }
            } catch (Exception e) {
                channel.sendMessage("Something went wrong with your ping.. Try again later!").queue();
            }
            
            channel.sendMessage(String.format("Pong! Your ping is `%s`.", ping)).queue();
        }
        
        // Simulated a coin flip
        else if(msg.startsWith(".flip")) {
            int number = _rnd.nextInt(1000001);
            
            // Just a silly thing in case it hits the last number in the random generation.
            if (number == 1000000) {
                channel.sendMessage("The coin landed on its edge! Wow, this is really a rare sight! Flip it again.").queue();
            } 
            
            // If number is divisible by 2, its heads, otherwise its tails.
            else {
                channel.sendMessage(number % 2 == 0 ? "Coin landed on heads!" : "Coin landed on tails!").queue();
            }
        }
        
        // Shows the uptime of the bot using smart time formatter
        else if(msg.startsWith(".uptime")) {
            DateTime now = DateTime.now();
            
            Period p = new Period(uptime, now);
            PeriodFormatterBuilder format = new PeriodFormatterBuilder()
                    .appendYears().appendSuffix(" year ", " years ")
                    .appendMonths().appendSuffix(" month ", "months ")
                    .appendWeeks().appendSuffix(" week ", "weeks ")
                    .appendDays().appendSuffix(" day ", "days ")
                    .appendHours().appendSuffix(" hour ", "hours ")
                    .appendMinutes().appendSuffix(" minute ", " minutes ")
                    .appendSeconds().appendSuffix(" second ", " seconds ");
            PeriodFormatter pf = format.toFormatter();
            
            channel.sendMessage(pf.print(p)).queue();
        }
        
        // Simulates a dice roll with n sides and throws it m times
        else if(msg.startsWith(".roll")) {
            try {
                if (msg.trim().length() <= 5) {
                    int number = _rnd.nextInt(6) + 1;
                    channel.sendMessage(String.format("You rolled %d.", number)).queue();
                } 
                else {
                    String expression = msg.substring(6).trim().toLowerCase();
                    String[] splitRolls = expression.split("d");
                    int rolls = Integer.parseInt(splitRolls[0]);
                    int sides = Integer.parseInt(splitRolls[1]);
                    
                    if (rolls >= 50 || sides >= 50) {
                        channel.sendMessage("Dont you think thats a bit too excessive?").queue();
                        return;
                    }
                    
                    if (rolls == 1) {
                        int number = _rnd.nextInt(6) + 1;
                        channel.sendMessage(String.format("You rolled %d.", number)).queue();
                        return;
                    }
                    
                    int[] rolled = new int[rolls];

                    for (int i = 0; i < rolls; i++) {
                        rolled[i] = _rnd.nextInt(sides) + 1;
                    }
                    
                    channel.sendMessage(getRollEmbed(jda, member, rolled)).queue();
                }
            } catch (IOException ex) {
                Logger.getLogger(EventListenerAdapter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception e) {
                channel.sendMessage("Your format was incorrect. The following format should be `.roll [how many rolls]d[how many sides]`.").queue();
            }
        }
        
        // Gets a random cat fact
        else if (msg.startsWith(".cats")) {
            try {
                URL url = new URL("https://catfacts-api.appspot.com/api/facts");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                
                JSONObject json = new JSONObject(br.readLine());
                String catfact = json.getJSONArray("facts").getString(0);
                channel.sendMessage(getCatsEmbed(jda, member, catfact)).queue();
            } catch (Exception e) {
                channel.sendMessage("Something is wrong with the API. Contact admin please.").queue();
            }
        }
        
        // Changes the avatar of the bot
        // Maybe i should add some layer of protection to this
        else if (msg.startsWith(".avatar")) {
            try {
                String url = msg.substring(8);
                InputStream is = new URL(url).openStream();
                channel.sendMessage("Avatar changed!").queue();
                jda.getSelfUser().getManager().setAvatar(Icon.from(is)).queue();
            } catch (Exception e) {
                channel.sendMessage("Please be careful about formatting. The correct format is `.avatar [link]`. Both `http` and `https` links are fine. Make sure it is a picture and that it ends on an appropriate format.").queue();
            }
        }
        
        // Sets the description of what a bot is currently 'playing'
        else if (msg.startsWith(".desc")) {
            if (member.getUser().getId().equals(ADMIN_ID)) {
                Presence p = jda.getPresence();
                String status = msg.length() < 6 ? "clear" : msg.substring(6);

                // If the message is equal to "clear", it clears the description
                p.setGame(status.toLowerCase().equals("clear") == true ? null : Game.of(status));
            }
            else {
                channel.sendMessage("You cannot change the description of the bot.").queue();
            }
        }
        
        // Gets the current weather for the received city/coutnry
        // Should maybe reformat the way it parses JSON and the whole function together
        else if (msg.startsWith(".weather")) {
            try {
                String location = msg.substring(9).trim();
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=e02836c415387e8045d4be9dd39aef54");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                
                JSONObject json = new JSONObject(br.readLine());
                
                channel.sendMessage(getWeatherEmbed(json, jda, member)).queue();
            } catch (MalformedURLException ex) {
                channel.sendMessage("API is down. Contact admin please.").queue();
            } catch (IOException ex) {
                channel.sendMessage("API is down. Contact admin please.").queue();
            } catch (JSONException ex) {
                channel.sendMessage("Something is wrong with the API. Contact admin.").queue();
            } catch (Exception e) {
                channel.sendMessage("Wrong format. The format has to be `.weather [name of city] OR [name of country]`.").queue();
            }
        }
        
        // Makes the bot to choose instead of a user in case they are in a dilema
        else if (msg.startsWith(".choose")) {
            if (msg.contains(",")) {
                String[] splitMessage = msg.split(",");
                String randomWord = splitMessage[_rnd.nextInt(splitMessage.length)].trim();
                channel.sendMessage(String.format(":crystal_ball: I choose %s!", randomWord)).queue();
            } else if (msg.contains("|")) {
                String[] splitMessage = msg.split("\\|");
                String randomWord = splitMessage[_rnd.nextInt(splitMessage.length)].trim();
                channel.sendMessage(String.format(":crystal_ball: I choose %s!", randomWord)).queue();
            } else {
                channel.sendMessage("Used the wrong separator. Decisions should be seperated with a `| (pipe)` or with a `, (comma)`.").queue();
            }
        }
        
        // Sets the color of the embed's bar for the server
        else if (msg.startsWith(".color")) {
            try {
                String rgbColors = msg.substring(7).trim();
                int r = 0;
                int g = 0;
                int b = 0;
                
                switch (rgbColors.toLowerCase()) {
                    case "black":
                        break;
                    case "red":
                        r = 255;
                        break;
                    case "green":
                        g = 255;
                        break;
                    case "blue":
                        b = 255;
                        break;
                    case "yellow":
                        r = 255;
                        g = 255;
                        break;
                    case "purple":
                        r = 255;
                        b = 255;
                        break;
                    case "cyan":
                        g = 255;
                        b = 255;
                        break;
                    case "white":
                        r = 255;
                        g = 255;
                        b = 255;
                        break;
                    default:
                        if(rgbColors.contains("|")) {
                            r = Integer.parseInt(rgbColors.split("\\|")[0].trim());
                            g = Integer.parseInt(rgbColors.split("\\|")[1].trim());
                            b = Integer.parseInt(rgbColors.split("\\|")[2].trim());
                            break;
                        }
                        else if (rgbColors.contains(",")) {
                            r = Integer.parseInt(rgbColors.split(",")[0].trim());
                            g = Integer.parseInt(rgbColors.split(",")[1].trim());
                            b = Integer.parseInt(rgbColors.split(",")[2].trim());
                            break;
                        }
                        
                        channel.sendMessage("The format was incorrect. The format should be `.color r|g|b` or `.color r, g, b`.").queue();
                        return;
                }
                
                Scanner sc = new Scanner(new File("files/servercolors.txt"));
                String line;
                boolean isNewColor = true;
                
                while (sc.hasNextLine()) {                    
                    line = sc.nextLine();
                    
                    if (line.contains(serverId)) {
                        updateServerColor(r, g, b);
                        isNewColor = false;
                        break;
                    }
                }
                
                if (isNewColor) {
                    FileWriter fw = new FileWriter("files/servercolors.txt", true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(String.format("%s %d|%d|%d", serverId, r, g, b) + System.lineSeparator());
                    bw.close();
                    fw.close();
                }
                
                channel.sendMessage(getColorEmbed(jda, member, isNewColor)).queue();
            } catch (FileNotFoundException ex) {
                channel.sendMessage("File not found. Contact admin please.").queue();
            } catch (IOException ex) {
                channel.sendMessage("File not found. Contact admin please.").queue();
            } catch (Exception e) {
                channel.sendMessage("The format was incorrect. The format should be `.color r|g|b` or `.color r, g, b`.").queue();
            }
        }
        
        // Pins a message to a channel
        else if (msg.startsWith(".pin")) {
            try {
                String id = msg.substring(5);
                System.out.println(id);
                channel.pinMessageById(id).queue(null, i -> channel.sendMessage("Message doesnt exist. Check in which channel you are right now.").queue());
            } catch (PermissionException e) {
                channel.sendMessage("Requires Permission.MESSAGE_READ and Permission.MESSAGE_MANAGE to pin a message.").queue();
            } catch (Exception e) {
                channel.sendMessage("No message id provided.").queue();
            }
        }
        
        // Changes the name of the bot
        else if (msg.startsWith(".name")) {
            if (user.getId().equals(ADMIN_ID)) {
                try {
                    String name = msg.substring(6);
                    jda.getSelfUser().getManager().setName(name).queue();
                } catch (Exception e) {
                    channel.sendMessage("Name cannot be empty.").queue();
                }
            } else {
                channel.sendMessage("You cannot change the name of the bot.").queue();
            }
        }
        
        // Changes the topic of the channel
        else if (msg.startsWith(".topic")) {
            
            // Requires TextChannel here instead of MessageChannel because .getManager()
            // isnt avaiable on MessageChannel object and im unsure how to get it in any other way
            try {
                event.getTextChannel().getManager().setTopic(msg.length() <= 7 ? "" : msg.substring(7)).queue();
            } catch (PermissionException e) {
                channel.sendMessage("Requires Permission.MANAGE_CHANNEL to change the topic of the channel.").queue();
            }
            
        }
        
        // Kicks the user from the server
        else if (msg.startsWith(".kick")) {
            
            // Try - catch checks if the bot has enough permission to kick someone
            // If - else checks if the user who called the command has enough permission to kick someone
            try {
                if (member.getPermissions().contains(Permission.KICK_MEMBERS) || member.getPermissions().contains(Permission.ADMINISTRATOR)) {
                    String id = message.getRawContent().replaceAll("\\D+", "");
                    GuildController gc = new GuildController(event.getGuild());
                    gc.kick(id).queue(i -> channel.sendMessage("User has been kicked from the server.").queue());
                } else {
                    channel.sendMessage("You dont have enough permission to issue that command.").queue();
                }
            } catch (PermissionException e) {
                channel.sendMessage("Requires Permission.KICK_MEMBERS to kick the user or you cannot kick the user because they are higher in hierarchy position than you.").queue();
            }
        }
        
        // Bans the user from the server
        else if (msg.startsWith(".ban")) {
            
            // Try - catch checks if the bot has enough permission to ban someone
            // If - else checks if the user who called the command has enough permission to ban someone
            try {
                if (member.getPermissions().contains(Permission.BAN_MEMBERS) || member.getPermissions().contains(Permission.ADMINISTRATOR)) {
                    String id = message.getRawContent().replaceAll("\\D+", "");
                    GuildController gc = new GuildController(event.getGuild());
                    gc.ban(id, 0).queue(i -> channel.sendMessage("User has been banned from the server.").queue());
                } else {
                    channel.sendMessage("You dont have enough permission to issue that command.").queue();
                }
            } catch (PermissionException e) {
                channel.sendMessage("Requires Permission.BAN_MEMBERS to ban the user or you cannot ban the user because they are higher in hierarchy position than you.").queue();
            }
        }
        
        // Gets a list of all the users on the server, how long have they been member for
        // Their nickname, their discord tag and their user id
        else if (msg.startsWith(".members")) {
            List<Member> listOfMembers = event.getGuild().getMembers();
            StringBuilder sb = new StringBuilder();
            String newLine = System.lineSeparator();
            String header = String.format("```cs%sFormat: User id | Date of joining | Nickname | Discord tag%s", newLine, newLine);
            List<String> listOfMembersFormated = new ArrayList<>();
            listOfMembersFormated = listOfMembers
                    .stream()
                    .map(i -> String.join(" | ", i.getUser().getId(), i.getJoinDate().toLocalDate().toString(), i.getEffectiveName(), i.getUser().getName() + "#" + i.getUser().getDiscriminator()))
                    .collect(Collectors.toList());
            listOfMembersFormated.forEach(i -> sb.append(i).append(newLine));
            
            // Fits the message into the max count of 2000 characters for private message
            while (sb.length() > 2) {
                String memberInfo = sb.length() > 2000 ? sb.substring(0, 1928) : sb.substring(0, sb.lastIndexOf(newLine) + 2);
                int index = memberInfo.lastIndexOf(newLine) + 2;
                memberInfo = memberInfo.substring(0, index);
                String messageToSend = String.format("%s%s```", header, memberInfo);
                user.openPrivateChannel().queue(i -> i.getUser().getPrivateChannel().sendMessage(messageToSend).queue());
                sb.delete(0, sb.length() > 2000 ? index : sb.lastIndexOf(newLine) + 2);
            }
        }
        
        else if (msg.startsWith(".suggestion")) {
            try {
                String suggestion = msg.substring(12).trim();
                FileWriter fw = new FileWriter("files/suggestions.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(String.format("Requested by: %s#%s from server %s. Server id: %s", member.getUser().getName(), member.getUser().getDiscriminator(), serverName, serverId) + System.lineSeparator());
                bw.write("------------------------------" + System.lineSeparator());
                bw.write(suggestion + System.lineSeparator() + System.lineSeparator());
                bw.close();
                fw.close();
                
                channel.sendMessage("Suggestion stored! Thank you for trying to help me improve my bot!").queue();
            } catch (IOException ex) {
                channel.sendMessage("File not found. Contact admin please.").queue();
            }
        }
        
        else if (msg.startsWith(".alch")) {
            try {
                URL url = new URL("https://rsbuddy.com/exchange/summary.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                JSONObject jsonBuddy = new JSONObject(br.readLine());
                JSONArray jsonItems = new JSONArray(getJSONString());
                ArrayList<RsItem> overallItems = new ArrayList<>();
                ArrayList<RsItem> buyItems = new ArrayList<>();
                
                int natsPriceOverall = jsonBuddy.getJSONObject("561").getInt("overall_average");
                int natsPriceBuy = jsonBuddy.getJSONObject("561").getInt("buy_average");
                int highAlch;
                int fullOverall;
                int fullBuy;
                int diffOverall;
                int diffBuy;
                
                for(int i = 0; i < jsonItems.length(); i++) {
                    JSONObject listItem = jsonItems.getJSONObject(i);
                    JSONObject buddyItem = jsonBuddy.getJSONObject(listItem.getString(("id")));
                    String itemName = buddyItem.getString("name");
                    int itemOverall = buddyItem.getInt("overall_average");
                    int itemBuy = buddyItem.getInt("buy_average");
                    
                    if (itemBuy == 0 || itemOverall == 0)
                        continue;
                    
                    highAlch = listItem.getInt("hialch");
                    fullOverall = itemOverall + natsPriceOverall;
                    fullBuy = itemBuy + natsPriceBuy;
                    diffOverall = highAlch - fullOverall;
                    diffBuy = highAlch - fullBuy;
                    
                    if (highAlch >= fullOverall && diffOverall <= 50)
                        overallItems.add(getRsItem(itemName, itemOverall, highAlch, (highAlch - fullOverall)));
                    if (highAlch >= fullBuy && diffBuy <= 50)
                        buyItems.add(getRsItem(itemName, itemBuy, highAlch, (highAlch - fullBuy)));
                }
                
                channel.sendMessage(getAlchEmbed(jda, member, overallItems, buyItems, natsPriceBuy, natsPriceOverall)).queue();
            } catch (IOException ex) {
                channel.sendMessage("File not found. Contact admin please.").queue();
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println(ex.getMessage());
                channel.sendMessage("Something went wrong. Contact admin please.").queue();
            }
        }
        
        else if (msg.startsWith(".merch")) {
            try {
                URL url = new URL("https://rsbuddy.com/exchange/summary.json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                JSONObject jsonBuddy = new JSONObject(br.readLine());
                ArrayList<RsItem> itemsBig = new ArrayList<>();
                ArrayList<RsItem> itemsSmall = new ArrayList<>();
                int diff = 0;
                
                for(int i = 1; i < jsonBuddy.length(); i++) {
                    if(!jsonBuddy.has(String.valueOf(i)))
                        continue;
                    JSONObject buddyItem = jsonBuddy.getJSONObject(String.valueOf(i));
                    String itemName = buddyItem.getString("name");
                    int itemBuy = buddyItem.getInt("buy_average");
                    int itemSell = buddyItem.getInt("sell_average");
                    
                    if (itemBuy == 0 || itemSell == 0)
                        continue;
                    
                    diff = itemSell - itemBuy;
                    
                    if (diff >= 50 && diff < 100)
                        itemsBig.add(getRsItem(itemName, itemBuy, itemSell, diff));
                    else if (diff > 0 && diff < 50)
                        itemsSmall.add(getRsItem(itemName, itemBuy, itemSell, diff));
                }
                
                channel.sendMessage(getFlipEmbed(jda, member, itemsBig, itemsSmall)).queue();
            } catch (IOException ex) {
                channel.sendMessage("File not found. Contact admin please.").queue();
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println(ex.getMessage());
                channel.sendMessage("Something went wrong. Contact admin please.").queue();
            } catch (PasteException ex) {
                Logger.getLogger(EventListenerAdapter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        else if (msg.startsWith(".slots")) {
            channel.sendMessage(String.format("%s %s", member.getEffectiveName(), SLOTS_MESSAGE[_rnd.nextInt(SLOTS_MESSAGE.length)])).queue();
            int size = SLOTS_ICONS.length;
            
            String firstFruit = SLOTS_ICONS[_rnd.nextInt(size)];
            String secondFruit = SLOTS_ICONS[_rnd.nextInt(size)];
            String thirdFruit = SLOTS_ICONS[_rnd.nextInt(size)];
            
            channel.sendMessage(String.format("[ %s | %s | %s ]", firstFruit, secondFruit, thirdFruit)).queue();
            channel.sendMessage(didYouWin(firstFruit, secondFruit, thirdFruit)).queue();
        }
        
    }    

    private MessageEmbed getWeatherEmbed(JSONObject json, JDA jda, Member member) throws IOException {
        EmbedBuilder eb = new EmbedBuilder();
        
        BigDecimal lon = json.getJSONObject("coord").has("lon") ? json.getJSONObject("coord").getBigDecimal("lon") : new BigDecimal(0.0);
        BigDecimal lat = json.getJSONObject("coord").has("lat") ? json.getJSONObject("coord").getBigDecimal("lat") : new BigDecimal(0.0);
        String weather = json.getJSONArray("weather").getJSONObject(0).has("main") ? json.getJSONArray("weather").getJSONObject(0).getString("main") : "N/A";
        BigDecimal temp = json.getJSONObject("main").has("temp") ? json.getJSONObject("main").getBigDecimal("temp") : new BigDecimal(0.0);
        int pressure = json.getJSONObject("main").has("pressure") ? json.getJSONObject("main").getInt("pressure") : 0;
        int humidity = json.getJSONObject("main").has("humidity") ? json.getJSONObject("main").getInt("humidity") : 0;
        BigDecimal windSpeed = json.getJSONObject("wind").has("speed") ? json.getJSONObject("wind").getBigDecimal("speed") : new BigDecimal(0.0);
        BigDecimal windDeg = json.getJSONObject("wind").has("wind") ? json.getJSONObject("wind").getBigDecimal("deg") : new BigDecimal(0.0);
        int clouds = json.getJSONObject("clouds").has("all") ? json.getJSONObject("clouds").getInt("all") : 0;
        String country = json.getJSONObject("sys").has("country") ? json.getJSONObject("sys").getString("country") : "N/A";
        String city = json.has("name") ? json.getString("name") : "N/A";
        
        SelfUser su = jda.getSelfUser();
        
        double tempC = temp.doubleValue() - 273.15;
        double tempF = (temp.doubleValue() * 1.8) - 459.67;
        
        eb.setAuthor(su.getName(), su.getAvatarUrl(), su.getAvatarUrl());
        eb.setColor(getServerColor());
        eb.setTitle(String.format("Weather for %s, %s :flag_%s:", city, country, country.toLowerCase()));
        eb.addField("Coordinates", String.format("Lon: %s | Lat: %s", lon.toString(), lat.toString()), false);
        eb.addField("Info", String.format("Weather: %s | Temperature: %.2f °C (%.2f °F)\nPressure: %d hPa | Humidity: %d %%", weather, tempC, tempF, pressure, humidity), false);
        eb.addField("Wind", String.format("Wind speed: %s | Wind degrees: %s °", windSpeed.toString(), windDeg.toString()), false);
        eb.addField("Clouds", String.format("Cloudy: %d %%", clouds), false);
        eb.setFooter(String.format("Requested by %s#%s", member.getUser().getName(), member.getUser().getDiscriminator()), member.getUser().getAvatarUrl());
        
        return eb.build();
    }

    private void updateServerColor(int r, int g, int b) {
        try {
            BufferedReader file = new BufferedReader(new FileReader("files/servercolors.txt"));
            String line;
            String input = "";

            while ((line = file.readLine()) != null) {
                if (line.contains(serverId)) {
                    input = input + serverId + " " + String.format("%d|%d|%d", r, g, b) + System.lineSeparator();
                }
                else {
                    input = input + line + System.lineSeparator();
                }
            }

            file.close();
            
            FileOutputStream fileOut = new FileOutputStream("files/servercolors.txt");
            fileOut.write(input.getBytes());
            fileOut.close();
        } catch (IOException e) {
            System.out.println("Problem reading file.");
        }
    }

    private MessageEmbed getColorEmbed(JDA jda, Member member, boolean isNewColor) throws IOException {
        EmbedBuilder eb = new EmbedBuilder();
        SelfUser su = jda.getSelfUser();
        
        eb.setAuthor(su.getName(), su.getAvatarUrl(), su.getAvatarUrl());
        
        eb = isNewColor ? eb.setTitle("Color added!") : eb.setTitle("Color updated!");
        
        eb.setColor(getServerColor());
        eb.setFooter(String.format("Requested by %s#%s", member.getUser().getName(), member.getUser().getDiscriminator()), member.getUser().getAvatarUrl());
        
        return eb.build();
    }

    private Color getServerColor() throws FileNotFoundException, IOException {
        try {
            BufferedReader file = new BufferedReader(new FileReader("files/servercolors.txt"));
            String line;

            while ((line = file.readLine()) != null) {
                if (line.contains(serverId)) {
                    String[] rgbColors = line.split(" ")[1].split("\\|");
                    int r = Integer.parseInt(rgbColors[0]);
                    int g = Integer.parseInt(rgbColors[1]);
                    int b = Integer.parseInt(rgbColors[2]);
                    Color c = new Color(r, g, b);
                    return c;
                }
            }
            
            file.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
        return Color.YELLOW;
    }

    private MessageEmbed getCatsEmbed(JDA jda, Member member, String catfact) throws IOException {
        EmbedBuilder eb = new EmbedBuilder();
        SelfUser su = jda.getSelfUser();
        
        eb.setAuthor(su.getName(), su.getAvatarUrl(), su.getAvatarUrl());
        eb.setTitle("Cat fact!");
        eb.addField("Did you know?", catfact, true);
        eb.setColor(getServerColor());
        eb.setFooter(String.format("Requested by %s#%s", member.getUser().getName(), member.getUser().getDiscriminator()), member.getUser().getAvatarUrl());
        
        return eb.build();
    }

    private MessageEmbed getRollEmbed(JDA jda, Member member, int... number) throws IOException {
        EmbedBuilder eb = new EmbedBuilder();
        SelfUser su = jda.getSelfUser();
        
        if (number.length == 1) {
            eb.addField("You rolled:", String.format("%d", number[0]), true);
        }
        else {
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < number.length; i++) {
                sb.append(String.format("%d, ", number[i]));
            }
            
            sb = sb.deleteCharAt(sb.lastIndexOf(","));
            eb.addField("You rolled:", String.format("%s", sb.toString()), true);
        }
        
        eb.setAuthor(su.getName(), su.getAvatarUrl(), su.getAvatarUrl());
        eb.setTitle("Rolled!");
        eb.setColor(getServerColor());
        eb.setFooter(String.format("Requested by %s#%s", member.getUser().getName(), member.getUser().getDiscriminator()), member.getUser().getAvatarUrl());
        
        return eb.build();
    }

    private String getJSONString() {
        try {
            File file = new File("files/rsitems.txt");
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            String str = new String(data, "UTF-8");
            return str;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EventListenerAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EventListenerAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    private RsItem getRsItem(String itemName, int price, int highAlch, int diff) {
        RsItem item = new RsItem(itemName, price, highAlch, diff);
        
        return item;
    }

    private MessageEmbed getAlchEmbed(JDA jda, Member member, ArrayList<RsItem> overallItems, ArrayList<RsItem> buyItems, int natsPriceBuy, int natsPriceOverall) throws IOException, ArrayIndexOutOfBoundsException {
        EmbedBuilder eb = new EmbedBuilder();
        SelfUser su = jda.getSelfUser();
        Collections.sort(overallItems);
        Collections.sort(buyItems);
        
        eb.addField("Format!", "Format for items below is the following:\r\n`Item name - Price on Grand Exchange - High Alchemy value - Difference`", false);
        
        if (!overallItems.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < overallItems.size(); i++) {
                sb.append(String.format("%d. %s - %d - %d - %d | ", (i + 1), overallItems.get(i).getName(), overallItems.get(i).getPrice(), overallItems.get(i).getHialch(), overallItems.get(i).getDiff()));
            }
            sb.deleteCharAt(sb.lastIndexOf("|"));
            eb.addField(String.format("Top %d for overall price!", overallItems.size()), sb.toString(), false);
        }
        else 
            eb.addField("No items found found for overall!", "Bad luck.. Try again in 30+ minutes.", false);

        if (!buyItems.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < buyItems.size(); i++) {
                sb.append(String.format("%d. %s - %d - %d - %d | ", (i + 1), buyItems.get(i).getName(), buyItems.get(i).getPrice(), buyItems.get(i).getHialch(), buyItems.get(i).getDiff()));
            }
            sb.deleteCharAt(sb.lastIndexOf("|"));
            eb.addField(String.format("Top %d for buy price!", buyItems.size()), sb.toString(), false);
        }
        else 
            eb.addField("No items found for buy!", "Bad luck.. Try again in 30+ minutes.", false);
        
        eb.setAuthor(su.getName(), su.getAvatarUrl(), su.getAvatarUrl());
        eb.setTitle(String.format("Nature rune price|Buy: %d, Overall: %d", natsPriceBuy, natsPriceOverall));
        eb.setDescription("Overall price is average of buying and selling.");
        eb.setColor(getServerColor());
        eb.setFooter(String.format("Requested by %s#%s", member.getUser().getName(), member.getUser().getDiscriminator()), member.getUser().getAvatarUrl());
        
        return eb.build();
    }

    private boolean messageIsBanned(String message, MessageChannel channel) {
        try {
            File f = new File("files/banned.txt");
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine()) {
                if(message.toLowerCase().contains(sc.next())) {
                    return true;
                }
            }
        } catch (FileNotFoundException ex) {
            channel.sendMessage("File not found. Please contact admin!").queue();
        } catch (Exception e) {
            channel.sendMessage("Something went wrong. Contact admin please.").queue();
        }
        
        return false;
    }

    private MessageEmbed getFlipEmbed(JDA jda, Member member, ArrayList<RsItem> itemsBig, ArrayList<RsItem> itemsSmall) throws IOException, PasteException {
        EmbedBuilder eb = new EmbedBuilder();
        SelfUser su = jda.getSelfUser();
        Collections.sort(itemsBig);
        Collections.sort(itemsSmall);
        
        PastebinPaste paste = new PastebinPaste();
        paste.setDeveloperKey(PASTEBIN_API_KEY);
        paste.setPasteExpireDate(PasteExpireDate.ONE_HOUR);
        paste.setVisibility(PastebinPaste.VISIBILITY_UNLISTED);
        
        eb.addField("Format!", "Format for items below is the following:\r\n`Item name - Buying for on Grand Exchange - Selling for on Grand Exchange`", false);
        
        if (!itemsBig.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < itemsBig.size(); i++) {
                sb.append(String.format("%d. %s - %d - %d%s", (i + 1), itemsBig.get(i).getName(), itemsBig.get(i).getPrice(), itemsBig.get(i).getHialch(), System.lineSeparator()));
            }
            
            paste.setPasteTitle("Big flips");
            paste.setContents(sb.toString());
            
            eb.addField("Wanna flip big?", paste.paste().getLink().toString(), true);
        }
        else 
            eb.addField("Awww!", "Bad luck.. Try again in 30+ minutes.", false);
        
        if (!itemsSmall.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < itemsSmall.size(); i++) {
                sb.append(String.format("%d. %s - %d - %d%s", (i + 1), itemsSmall.get(i).getName(), itemsSmall.get(i).getPrice(), itemsSmall.get(i).getHialch(), System.lineSeparator()));
            }
            
            paste.setPasteTitle("Small flips");
            paste.setContents(sb.toString());
            
            eb.addField("Wanna flip small?", paste.paste().getLink().toString(), true);
        }
        else 
            eb.addField("Awww!", "Bad luck.. Try again in 30+ minutes.", false);
        
        eb.setAuthor(su.getName(), su.getAvatarUrl(), su.getAvatarUrl());
        eb.setTitle("What to flip? Heres your answer!");
        eb.setDescription("Big is for difference `>= 50 and < 100`, small is `< 50`. May not be accurate and/or fast.");
        eb.setColor(getServerColor());
        eb.setFooter(String.format("Requested by %s#%s", member.getUser().getName(), member.getUser().getDiscriminator()), member.getUser().getAvatarUrl());
        
        return eb.build();
    }

    private String didYouWin(String firstFruit, String secondFruit, String thirdFruit) {
        if(firstFruit.equals(secondFruit) && firstFruit.equals(thirdFruit))
            return "Congratulations! You won!";
        
        if (firstFruit.equals(secondFruit) || firstFruit.equals(thirdFruit) || secondFruit.equals(thirdFruit))
            return "Aww, that was close. Better try again!";
        
        return "You didnt win anything, better luck next time..";
    }

    // Roll, flip, weather, cats, color, uptime, topic, pin, kick, ban
    // Stats, slots, members
    // Should order these alphabetically or ones without format first then the ones with format
    private String getListOfCommands() {
        StringBuilder sb = new StringBuilder();
        String newLine = System.lineSeparator();
        
        sb.append("```css").append(newLine);
        sb.append(String.format(".roll - Rolls a dice for you!%s\tFormat - .roll [how many rolls]d[how many sides]%s", newLine, newLine));
        sb.append(".flip - Flips a coin for you!").append(newLine);
        sb.append(".cats - Shows you a random cat fact (which you probably didnt know about)!").append(newLine);
        sb.append(String.format(".color - Sets a color for the message embed!%s\tFormat - .color r|g|b%s OR r, g, b", newLine, newLine));
        sb.append(".uptime - Gives you the uptime of the bot!").append(newLine);
        sb.append(String.format(".weather - Gives you the current weather statistics!%s\tFormat - .weather [name of city] OR [name of country]%s", newLine, newLine));
        sb.append(String.format(".choose - Makes a decision for you!%s\tFormat - .choose a|b|c or a, b, c%s", newLine, newLine));
        sb.append(".slots - Rolls a slot, just like a real slot machine!").append(newLine);
        sb.append(".topic - Changes the topic of the channel! Leave it empty to clear the topic!").append(newLine);
        sb.append(".pin - Pins a message by message id!").append(newLine);
        sb.append(".members - Shows stats of all members in the server!").append(newLine);
        sb.append(String.format(".kick - Kicks an user from the server!%s\tFormat - .kick [@mention] or [user id]%s", newLine, newLine));
        sb.append(String.format(".ban - Bans an user from the server!%s\tFormat - .ban [@mention] or [user id]%s", newLine, newLine));
        sb.append("```");
        
        return sb.toString();
    }
}
