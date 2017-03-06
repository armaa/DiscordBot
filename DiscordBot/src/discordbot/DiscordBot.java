/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discordbot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.joda.time.DateTime;

/**
 *
 * @author armaa
 */
public class DiscordBot {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            JDA jda = new JDABuilder(AccountType.BOT).setToken("").addListener(new EventListenerAdapter(DateTime.now())).buildBlocking();
        } catch (Exception e) {
            
        }
    }
    
}
