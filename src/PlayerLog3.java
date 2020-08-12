/*
current goals:
1. fix unknown command bug: done, but my solution is quite stupid
2. command to add players: done
3. notifications: done-ish
    - create boolean variable and have commands that turn on/off notifications for both specific players and every player: done
    - have it check to see if a specific player is online or not every minute, and to say "<playerName> got online!" or "<playerName> got offline!" when they get on/offline: done-ish
4. ui/app thing and actual notifications on computer
5. bug: last seen only works within the same day, whoops
6. bug: once the notifications start, they repeat every minute, but they change if the player goes offline
7. bug: cannot quit once notifications are turned on
Deadline: 8/12

other possible things to add
1. heli/cargo log would be cool, might only be possible on stevious tho, sadly. look into it
 */
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
public class PlayerLog3 {
    public static  void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);

        System.out.print("How many players would you like to add to your watchlist? ");
        int playerNum = scan.nextInt();

        ArrayList<Player3> playerList = new ArrayList<>();
        for(int i=0; i < playerNum; i++) {
            System.out.print("List the name of player " + (i+1) + ": ");
            String playerName = scan.next();
            System.out.print("List the Battlemetrics link of " + playerName + ": ");
            String playerLink = scan.next();
            Player3 newPlayer = new Player3(playerName, playerLink, false);
            playerList.add(newPlayer);
        }

        //commands list
        getCommandList();
        while(true) {
            String command = scan.nextLine(); //collects user's command
            Player3 cmdPlayer = new Player3("", "", false); //player possibly used in command

            //checks to make sure that a player possibly in the command is an actual available player
            for (Player3 player : playerList) {
                if (command.contains(player.getName())) {
                    cmdPlayer = new Player3(player.getName(), player.getLink(), player.isNotificationsToggle());
                }
            }

            //prints the command list
            if (command.equals("commandList();")) {
                getCommandList();
            }

            //allows the user to add more players to the watchlist without restarting the program
            else if (command.equals("addPlayers();")) {
                System.out.print("How many players would you like to add to your watchlist? ");
                playerNum = scan.nextInt();
                for(int i=0; i < playerNum; i++) {
                    System.out.print("List the name of player " + (i+1) + ": ");
                    String playerName = scan.next();
                    System.out.print("List the Battlemetrics link of " + playerName + ": ");
                    String playerLink = scan.next();
                    Player3 newPlayer = new Player3(playerName, playerLink, false);
                    playerList.add(newPlayer);
                }
                getCommandList();
            }

            //turns on/off notifications for a specific player
            else if (command.contains(".notifications();")) {
                for (Player3 player3 : playerList) {
                    if (player3.getName().equals(cmdPlayer.getName())) {
                        player3.setNotificationsToggle(!player3.isNotificationsToggle());
                        System.out.println("Notificaitons: " + player3.isNotificationsToggle());
                    }
                }
            }

            //turns on notifications for all players, or off notifications if all notifications are already on
            else if (command.equals("allNotifications();")) {
                int falseCounter=0;
                for (Player3 player3 : playerList) {
                    if (!player3.isNotificationsToggle()) {
                        falseCounter++;
                    }
                }
                if(falseCounter>0) {
                    for (Player3 player3 : playerList) {
                        if (!player3.isNotificationsToggle()) {
                            player3.setNotificationsToggle(true);
                        }
                    }
                } else {
                    for (Player3 player3 : playerList) {
                        player3.setNotificationsToggle(false);
                    }
                }
            }

            //prints <player>.isOnline();
            else if (command.contains(".isOnline();")) {
                if (!cmdPlayer.getName().equals("")) {
                    System.out.println(cmdPlayer.isOnline());
                } else {
                    System.out.println("An error occurred. Make sure you spelled the name correctly");
                }
            }

            //prints <player>.lastSeen();
            else if (command.contains(".lastSeen();")) {
                if (!cmdPlayer.getName().equals("")) {
                    //gets current time that battlemetrics is using
                    java.util.TimeZone tz = java.util.TimeZone.getTimeZone("GMT");
                    java.util.Calendar c = java.util.Calendar.getInstance(tz);

                    //converts to minutes
                    int currentTimeInMin = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(java.util.Calendar.MINUTE); //Battlemetrics time
                    int logTimeInMin = cmdPlayer.lastSeen();

                    //subtracts logTimeInMin from currentTimeInMin to find out how long the player was last online if the time offline is > 0
                    if (logTimeInMin > 0) {
                        int timeOffline = currentTimeInMin - logTimeInMin;
                        System.out.println(cmdPlayer.getName() + " got offline " + timeOffline / 60 + " hours and " + timeOffline % 60 + " minutes ago");
                    //if time offline <= 0, player is online
                    } else {
                        System.out.println("The player is currently online");
                    }
                } else {
                    System.out.println("An error occurred. Make sure you spelled the name correctly");
                }
            }

            //removes all player files and quits the program
            else if (command.contains("quit();")) { //works
                for (Player3 player3 : playerList) {
                    fileDeleter(player3.getName() + ".txt");
                }
                break;
            }

            //shits retarded and would go ahead and print "Unknown command" before any commands are written but this shit fixes it
            else if (command.isEmpty()) {
                System.out.print("");
            }

            //prints "Unknown command" if the command the user entered isn't recognized
            else {
                System.out.println("Unknown command");
            }

            for (Player3 player3 : playerList) {
                if (player3.isNotificationsToggle()) {
                    player3.notifications();
                }
            }
        }
    }

    //downloads webpage code into a .txt file
    public static void webPageDownloader(String name, String link) throws IOException {
        URL url = new URL(link);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        BufferedWriter writer = new BufferedWriter(new FileWriter(name + ".txt"));
        String line = "";
        while((line = reader.readLine()) != null) {
            writer.write(line);
        }
        reader.close();
        writer.close();
    }

    //deletes file
    public static void fileDeleter(String fileName) throws IOException {
        Files.deleteIfExists(Path.of(fileName));
    }

    //just so I dont have to update 5 things everytime i add a command
    public static void getCommandList() {
        System.out.println("Commands:\n    1. commandList();\n    2. addPlayers();\n    3. <playerName>.notifications();\n    4. allNotifications();\n    5. <playerName>.isOnline();\n" +
                "    6. <playerName>.lastSeen();\n    7. quit();");
    }
}