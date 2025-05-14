import java.util.*;
import java.util.concurrent.*;
import java.time.Instant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;


public class Game {

/* Object: JavaScript's game object is replaced with a Java Game class. Static members are used to hold the data. */

    private static final Random random = new Random();
    private static final Queue<String> queue = new LinkedList<>(); // Use a Queue for FIFO behavior
    private static ScheduledExecutorService clock; // Use ScheduledExecutorService for timer
/* 
clock (Worker): JavaScript's Worker is replaced with Java's ScheduledExecutorService.
This is the appropriate way to handle timed events in Java.
I've used a singleThreadScheduledExecutor as an example.
The scheduleAtFixedRate method is used to call the timer1Timer method repeatedly.
*/
    // Placeholder for TaskBar - you'll need to implement this based on your needs
    private static class TaskBar {
        public static boolean done() {
            // Replace with your actual logic to check if the task is done
            return true; // Example: Always true
        }
    }

/*
timeGetTime(): new Date().getTime() becomes Instant.now().toEpochMilli().
This gets the current time in milliseconds.
*/
    public static long timeGetTime() {
        return Instant.now().toEpochMilli(); // Use Instant for current time in milliseconds
    }

/*
StartTimer() and StopTimer(): 
These methods now manage the ScheduledExecutorService.
The example code in main shows how to start and stop the timer.
*/
    public static void startTimer() {
        if (clock == null) {
            clock = Executors.newSingleThreadScheduledExecutor(); // Create a single-thread scheduler

            // Schedule the timer task to run repeatedly after a delay (0 here, adjust as needed)
            clock.scheduleAtFixedRate(Game::timer1Timer, 0, 1000, TimeUnit.MILLISECONDS); // Example: Every 1000ms (1 second)
        }
    }


    public static void stopTimer() {
        if (clock != null) {
            clock.shutdown(); // Stop the scheduler
            try {
                clock.awaitTermination(1, TimeUnit.SECONDS); // Wait for tasks to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
            }
            clock = null; // Reset the clock
        }
    }

/*
Q() (Queue): 
JavaScript arrays used as queues are replaced with Java's LinkedList,
which implements the Queue interface for FIFO behavior. push becomes offer.
*/
    public static void q(String s) {
        queue.offer(s); // Offer adds to the queue (like push)
        dequeue();
    }

/* Dequeue():
This method now processes the queue. queue.poll() removes and returns the first element.
You'll need to put your actual processing logic in the while loop.
*/
    private static void dequeue() {
        // Process elements from the queue as needed
        while (!queue.isEmpty()) {
            String item = queue.poll(); // Poll removes and returns the head of the queue
            System.out.println("Dequeued: " + item); // Example: Process the item
        }
    }

    public static boolean taskDone() {
        return TaskBar.done();
    }
/* Odds(), RandSign(), RandomLow(): Java's Random class is used. */
    public static boolean odds(int chance, int outOf) {
        return random.nextInt(outOf) < chance;
    }

    public static int randSign() {
        return random.nextInt(2) * 2 - 1;
    }

    public static int randomLow(int below) {
        return Math.min(random.nextInt(below), random.nextInt(below));
    }
/* PickLow():
A generic method is used to handle lists of any type.
Be sure to pass a List (like ArrayList) of objects to this method.
*/
    public static <T> T pickLow(List<T> s) { // Use List<T> for generic type
        return s.get(randomLow(s.size()));
    }
/* Copy():
Java's substring method is used.
I've added a check to prevent IndexOutOfBoundsException if l is too large.
*/
    public static String copy(String s, int b, int l) {
        return s.substring(b - 1, Math.min(b + l - 1, s.length())); // Handle potential out-of-bounds
    }

    public static int length(String s) {
        return s.length();
    }
/* 
Starts() and Ends():
Java's String class has startsWith() and endsWith() methods.
*/
    public static boolean starts(String s, String pre) {
        return s.startsWith(pre);
    }

    public static boolean ends(String s, String e) {
        return copy(s, 1 + length(s) - length(e), length(e)).equals(e);
    }

/* plural(): Handles the pluralization rules. */
    public static String plural(String s) {
        if (ends(s, "y")) {
            return copy(s, 1, length(s) - 1) + "ies";
        } else if (ends(s, "us")) {
            return copy(s, 1, length(s) - 2) + "i";
        } else if (ends(s, "ch") || ends(s, "x") || ends(s, "s") || ends(s, "sh")) {
            return s + "es";
        } else if (ends(s, "f")) {
            return copy(s, 1, length(s) - 1) + "ves";
        } else if (ends(s, "man") || ends(s, "Man")) {
            return copy(s, 1, length(s) - 2) + "en";
        } else {
            return s + "s";
        }
    }

/* split():
Splits a string by a separator.
I've added a check to prevent ArrayIndexOutOfBoundsException.
It returns an empty string "" if the field index is invalid.
*/
    public static String split(String s, int field, String separator) {
        String[] parts = s.split(separator == null ? "|" : separator);
        if (field >= 0 && field < parts.length) {
            return parts[field];
        }
        return ""; // Or throw an exception if you prefer
    }
/* indefinite() and definite(): Handle indefinite and definite articles. */
    public static String indefinite(String s, int qty) {
        if (qty == 1) {
            String vowels = "AEIOUÜaeiouü";
            if (vowels.indexOf(s.charAt(0)) > -1) {
                return "an " + s;
            } else {
                return "a " + s;
            }
        } else {
            return qty + " " + plural(s);
        }
    }

    public static String definite(String s, int qty) {
        if (qty > 1) {
            s = plural(s);
        }
        return "the " + s;
    }
/* prefix(): A more generic prefix function. */
    public static String prefix(String[] a, int m, String s, String sep) {
        if (sep == null) sep = " ";
        m = Math.abs(m);
        if (m < 1 || m > a.length) return s;
        return a[m - 1] + sep + s;
    }
/* sick(), young(), big(): Use the prefix() function. */
    public static String sick(int m, String s) {
        m = 6 - Math.abs(m);
        return prefix(new String[]{"dead", "comatose", "crippled", "sick", "undernourished"}, m, s, null);
    }

    public static String young(int m, String s) {
        m = 6 - Math.abs(m);
        return prefix(new String[]{"foetal", "baby", "preadolescent", "teenage", "underage"}, m, s, null);
    }

    public static String big(int m, String s) {
        return prefix(new String[]{"greater", "massive", "enormous", "giant", "titanic"}, m, s, null);
    }
/* special(): Handles the special prefix cases. */
    public static String special(int m, String s) {
        if (s.contains(" ")) {
            return prefix(new String[]{"veteran", "cursed", "warrior", "undead", "demon"}, m, s, null);
        } else {
            return prefix(new String[]{"Battle-", "cursed ", "Were-", "undead ", "demon "}, m, s, "");
    }
}

/* interplotCinematic():
The core cinematic logic.
I've replaced the calls to NamedMonster, GetI, ImpressiveGuy, and BoringItem with placeholder methods.
*/
    public static void interplotCinematic() {
        int randomCase = random.nextInt(3);
        switch (randomCase) {
            case 0:
                q("task|1|Exhausted, you arrive at a friendly oasis in a hostile land");
                q("task|2|You greet old friends and meet new allies");
                q("task|2|You are privy to a council of powerful do-gooders");
                q("task|1|There is much to be done. You are chosen!");
                break;
            case 1:
                q("task|1|Your quarry is in sight, but a mighty enemy bars your path!");
                String nemesis = namedMonster(getI(traits, "Level") + 3); // Replace with your NamedMonster and getI implementations
                q("task|4|A desperate struggle commences with " + nemesis);
                int s = random.nextInt(3);
                for (int i = 1; i <= random.nextInt(1 + game.act + 1); ++i) { // Make sure game.act is defined
                    s += 1 + random.nextInt(2);
                    switch (s % 3) {
                        case 0:
                            q("task|2|Locked in grim combat with " + nemesis);
                            break;
                        case 1:
                            q("task|2|" + nemesis + " seems to have the upper hand");
                            break;
                        case 2:
                            q("task|2|You seem to gain the advantage over " + nemesis);
                            break;
                    }
                }
                q("task|3|Victory! " + nemesis + " is slain! Exhausted, you lose consciousness");
                q("task|2|You awake in a friendly place, but the road awaits");
                break;
            case 2:
                String nemesis2 = impressiveGuy(); // Replace with your ImpressiveGuy implementation
                q("task|2|Oh sweet relief! You've reached the kind protection of " + nemesis2);
                q("task|3|There is rejoicing, and an unnerving encounter with " + nemesis2 + " in private");
                q("task|2|You forget your " + boringItem() + " and go back to get it"); // Replace with your BoringItem implementation
                q("task|2|What's this!? You overhear something shocking!");
                q("task|2|Could " + nemesis2 + " be a dirty double-dealer?");
                q("task|3|Who can possibly be trusted with this news!? -- Oh yes, of course");
                break;
        }
        q("plot|1|Loading");
    }

/* strToInt() and intToStr():
Added these utility functions to convert between strings and integers.
I've included error handling in strToInt() in case the string isn't a valid integer.
*/
    public static String strToInt(String s) {
        try {
            return String.valueOf(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return "0"; // Or handle the error as you see fit
        }
    }

    public static String intToStr(int i) {
        return String.valueOf(i);
    }

/* namedMonster() and impressiveGuy():
Converted to Java.
Assumes you have lists of strings in the K class for K.Monsters, K.ImpressiveTitles, and K.Races.
*/
    public static String namedMonster(int level) {
        int lev = 0;
        String result = "";
        for (int i = 0; i < 5; ++i) {
            String m = pick(K.Monsters); // Assuming K.Monsters is a List<String>
            if (result.isEmpty() || (Math.abs(level - Integer.parseInt(split(m, 1))) < Math.abs(level - lev))) {
                result = split(m, 0);
                lev = Integer.parseInt(split(m, 1));
            }
        }
        return generateName() + " the " + result; // Assuming generateName() is defined
    }

    public static String impressiveGuy() {
        if (random.nextInt(2) == 0) {
            return "the " + pick(K.ImpressiveTitles) + " of the " + plural(split(pick(K.Races), 0)); // Assuming K.Races is a List<String>
        } else {
            return pick(K.ImpressiveTitles) + " " + generateName() + " of " + generateName();
        }
    }

/* monsterTask():
This is the most complex function. It's now converted to Java.
I have made the following changes:
game.questmonster is assumed to be a String.
K.Monsters, K.Races, K.Klasses, and K.Titles are assumed to be List<String>.
The function now returns a Map<String, Object> containing the description and level.
This is a cleaner way to return multiple values.
*/
    public static Map<String, Object> monsterTask(int level) {
        boolean definite = false;
        for (int i = level; i >= 1; --i) {
            if (odds(2, 5)) {
                level += randSign();
            }
        }
        if (level < 1) level = 1;

        String monster, levStr;
        int lev;
        if (odds(1, 25)) {
            monster = " " + split(pick(K.Races), 0);
            if (odds(1, 2)) {
                monster = "passing" + monster + " " + split(pick(K.Klasses), 0); // Assuming K.Klasses is a List<String>
            } else {
                monster = pickLow(K.Titles) + " " + generateName() + " the" + monster; // Assuming K.Titles is a List<String>
                definite = true;
            }
            levStr = String.valueOf(level);
            monster = monster + "|" + levStr + "|*";
        } else if (game.questmonster != null && odds(1, 4)) { // Assuming game.questmonster is a String
            monster = K.Monsters.get(game.questmonsterindex); // Assuming K.Monsters is a List<String>
            levStr = split(monster, 1);
            lev = Integer.parseInt(levStr);
        } else {
            monster = pick(K.Monsters);
            levStr = split(monster, 1);
            lev = Integer.parseInt(levStr);
            for (int ii = 0; ii < 5; ++ii) {
                String m1 = pick(K.Monsters);
                int lev1 = Integer.parseInt(split(m1, 1));
                if (Math.abs(level - lev1) < Math.abs(level - lev)) {
                    monster = m1;
                    lev = lev1;
                }
            }
        }

        String result = split(monster, 0);
        game.task = "kill|" + monster;

        int qty = 1;
        if (level - lev > 10) {
            qty = (int) Math.floor((level + random.nextInt(Math.max(lev, 1))) / (double) Math.max(lev, 1));
            if (qty < 1) qty = 1;
            level = (int) Math.floor(level / (double) qty);
        }

        if (level - lev <= -10) {
            result = "imaginary " + result;
        } else if (level - lev < -5) {
            int i = 10 + (level - lev);
            i = 5 - random.nextInt(i + 1);
            result = sick(i, young(lev - level - i, result));
        } else if ((level - lev < 0) && (random.nextInt(2) == 1)) {
            result = sick(level - lev, result);
        } else if (level - lev < 0) {
            result = young(level - lev, result);
        } else if (level - lev >= 10) {
            result = "messianic " + result;
        } else if (level - lev > 5) {
            int i = 10 - (level - lev);
            i = 5 - random.nextInt(i + 1);
            result = big(i, special(level - lev - i, result));
        } else if ((level - lev > 0) && (random.nextInt(2) == 1)) {
            result = big(level - lev, result);
        } else if (level - lev > 0) {
            result = special(level - lev, result);
        }

        lev = level;
        level = lev * qty;

        if (!definite) result = indefinite(result, qty);

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("description", result);
        taskData.put("level", level);
        return taskData;
    }


/* lowerCase() and properCase(): Straightforward conversions to Java. Added a null check to properCase(). */

    public static String lowerCase(String s) {
        return s.toLowerCase();
    }

    public static String properCase(String s) {
        if (s == null || s.isEmpty()) {
            return ""; // Or throw an exception
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static int equipPrice() {
        return   5 * getI(traits, "Level") * getI(traits, "Level") +
                10 * getI(traits, "Level") +
                20;
    }
/* 
dequeue(): 
complex function. Key changes:
String splitting now uses split("\\|") to correctly split on the "|" character.
The queue is handled using game.queue.remove(0) after processing the head of the queue.
*/
    public static void dequeue() {
        while (taskDone()) {
            String taskType = split(game.task, 0);
            if (taskType.equals("kill")) {
                if (split(game.task, 3).equals("*")) {
                    winItem(); // Implement winItem()
                } else if (!split(game.task, 3).isEmpty()) {
                    add(inventory, lowerCase(split(game.task, 1) + " " +
                            properCase(split(game.task, 3))), 1);
                }
            } else if (game.task.equals("buying")) {
                add(inventory, "Gold", -equipPrice());
                winEquip(); // Implement winEquip()
            } else if (game.task.equals("market") || game.task.equals("sell")) {
                if (game.task.equals("sell")) {
                    int amt = getI(inventory, 1) * getI(traits, "Level");
                    if (inventory.label(1).contains(" of ")) {
                        amt *= (1 + randomLow(10)) * (1 + randomLow(getI(traits, "Level")));
                    }
                    inventory.remove1(); // Implement remove1()
                    add(inventory, "Gold", amt);
                }
                if (inventory.length() > 1) {
                    inventory.scrollToTop(); // Implement scrollToTop()
                    task("Selling " + indefinite(inventory.label(1), getI(inventory, 1)), 1 * 1000);
                    game.task = "sell";
                    break;
                }
            }

            String old = game.task;
            game.task = "";
            if (!game.queue.isEmpty()) {
                String[] parts = game.queue.get(0).split("\\|");  // Split using "|"
                String a = parts[0];
                int n = Integer.parseInt(parts[1]);
                String s = parts[2];

                game.queue.remove(0); // Remove from queue

                if (a.equals("task") || a.equals("plot")) {
                    if (a.equals("plot")) {
                        completeAct(); // Implement completeAct()
                        s = "Loading " + game.bestplot;
                    }
                    task(s, n * 1000);
                } else {
                    throw new IllegalArgumentException("bah!" + a);
                }
            } else if (encumBar.done()) { // Implement EncumBar and its methods
                task("Heading to market to sell loot", 4 * 1000);
                game.task = "market";
            } else if (!old.startsWith("kill|") && !old.equals("heading")) {
                if (getI(inventory, "Gold") > equipPrice()) {
                    task("Negotiating purchase of better equipment", 5 * 1000);
                    game.task = "buying";
                } else {
                    task("Heading to the killing fields", 4 * 1000);
                    game.task = "heading";
                }
            } else {
                int nn = getI(traits, "Level");
                Map<String, Object> t = monsterTask(nn);
                int inventoryLabelAlsoGameStyleTag = 3; // You might want to make this configurable.
                nn = (int) Math.floor((2.0 * inventoryLabelAlsoGameStyleTag * (int) t.get("level") * 1000) / nn);
                task("Executing " + t.get("description"), nn);
            }
        }
    }

    public static void put(MyList list, String key, Object value) {
        if (key == null) {
            return; // Or throw an exception
        }

        if (list.fixedKeys) {
            // Assuming game is a Map<String, Object>
            if (!game.containsKey(list.id)) {
                game.put(list.id, new HashMap<>());
            }
            ((Map<String, Object>) game.get(list.id)).put(key, value);
        } else {
            List<List<Object>> data = (List<List<Object>>) game.get(list.id);
            if(data == null){
                data = new ArrayList<>();
                game.put(list.id, data);
            }
            int i = 0;
            for (; i < data.size(); ++i) {
                if (data.get(i).get(0).equals(key)) {
                    data.get(i).set(1, value);
                    break;
                }
            }
            if (i == data.size()) {
                data.add(Arrays.asList(key, value));
            }
        }

        list.putUI(key, value); // Implement PutUI()

        if (key.equals("STR")) {
            encumBar.reset(10 + (int) value, encumBar.position()); // Assuming EncumBar is defined
        }

        if (list == inventory) {
            int cubits = 0;
            // Assuming Inventory is a MyList
            List<List<Object>> invData = (List<List<Object>>) game.get("Inventory");

            if(invData != null) {
                for (List<Object> item : invData.subList(1, invData.size())) {
                    cubits += Integer.parseInt(item.get(1).toString());
                }
            }
            encumBar.reposition(cubits);
        }
    }


    public static class ProgressBar {
        String id;
        Element bar; // Use org.jsoup.nodes.Element
        String tmpl;

        public ProgressBar(String id, String tmpl) {
            this.id = id;
            // Use Jsoup to select elements.  This assumes you have the HTML loaded.
            // You might need to parse it first using Jsoup.parse(htmlString).
            this.bar =  getBarElementById(id); // Replace with appropriate Jsoup selection.

            this.tmpl = tmpl;
        }

        private Element getBarElementById(String id) {
            try {
                // Assuming you have the HTML content in a string variable named 'htmlContent'
                String htmlContent = "<div id=\"" + id + "\"><div class=\"bar\"></div><div class=\"hint\"></div></div>";
                Document doc = Jsoup.parse(htmlContent);
                return doc.select("#" + id + " > .bar").first();
            } catch (Exception e) {
                e.printStackTrace(); // Handle or log the exception as needed
                return null;
            }

        }


        public int max() {
            return (int) game.get(this.id + ".max"); // Use appropriate casting
        }

        public int position() {
            return (int) game.get(this.id + ".position"); // Use appropriate casting
        }

        public void reset(int newmax, Integer newposition) {
            game.put(this.id + ".max", newmax); // Store with key including ID
            reposition(newposition == null ? 0 : newposition);
        }

        public void reposition(int newpos) {
            int maxVal = max();
            game.put(this.id + ".position", Math.min(newpos, maxVal));

            // Recompute hint
            double percent = maxVal == 0 ? 0 : (100.0 * position()) / maxVal;
            game.put(this.id + ".percent", percent);
            game.put(this.id + ".remaining", maxVal - position());
            game.put(this.id + ".time", roughTime(maxVal - position())); // Implement roughTime()
            game.put(this.id + ".hint", template(this.tmpl, game.get(this.id))); // Implement template()

            // Update UI (using Jsoup)
            if (this.bar != null) {
                this.bar.attr("style", "width: " + percent + "%"); // Set width using style
                Element hintElement = this.bar.parent().select(".hint").first();
                if (hintElement != null) {
                    hintElement.text(game.get(this.id + ".hint").toString());
                }
            }
        }

        public void increment(int inc) {
            reposition(position() + inc);
        }

        public boolean done() {
            return position() >= max();
        }

        public void load() {
            reposition(position());
        }
    }

    public static String key(Element tr) {
        return tr.child(0).text();
    }

    public static String value(Element tr) {
        return tr.child(tr.childrenSize() - 1).text(); // Get last child
    }

    public static class ListBox {
        String id;
        Element box; // Use org.jsoup.nodes.Element
        int columns;
        List<String> fixedKeys;

        public ListBox(String id, int columns, List<String> fixedkeys) {
            this.id = id;
            this.box = getTbodyElementById(id); // Replace with appropriate Jsoup selection.
            this.columns = columns;
            this.fixedKeys = fixedkeys;
        }

        private Element getTbodyElementById(String id) {
            try {
                String htmlContent = "<table id = \"" + id + "\"><tbody></tbody></table>";
                Document doc = Jsoup.parse(htmlContent);
                return doc.select("tbody#" + id + ", #" + id + " tbody").first();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public Element addUI(String caption) {
            if (this.box == null) return null;
            Element tr = new Element("tr").append("<td><input type=checkbox disabled> " + caption + "</td></tr>");
            this.box.append(tr);
            // Implement scrollIntoView if needed (not directly available in Jsoup)
            return tr;
        }

        public void clearSelection() {
            if (this.box != null) {
                this.box.select("tr").removeClass("selected");
            }
        }

        public void putUI(String key, Object value) {
            if (this.box == null) return;
            Elements items = this.rows().stream().filter(tr -> key(tr).equals(key)).collect(org.jsoup.select.Collector.toList());
            Element item = items.isEmpty() ? null : items.get(0);
            if (item == null) {
                item = new Element("tr").append("<td>" + key + "</td><td></td></tr>");
                this.box.append(item);
            }

            item.child(item.childrenSize() - 1).text(value == null ? "" : value.toString());
            item.addClass("selected");
            // Implement scrollIntoView if needed
        }

        public void scrollToTop() {
            // Implement scrolling if needed (not directly available in Jsoup)
        }

        public Elements rows() {
            return this.box == null ? new Elements() : this.box.select("tr").has("td");
        }

        public void checkAll(boolean butlast) {
            if (this.box != null) {
                Elements checkboxes = this.rows().select("input:checkbox");
                if (butlast) {
                    checkboxes.not(":last-child").attr("checked", "true");
                } else {
                    checkboxes.attr("checked", "true");
                }
            }
        }

        public int length() {
            return fixedKeys != null ? fixedKeys.size() : ((List<List<Object>>) game.get(this.id)).size();
        }

        public void remove0() {
            List<List<Object>> data = (List<List<Object>>) game.get(this.id);
            if (data != null && !data.isEmpty()) {
                data.remove(0);
            }
            if (this.box != null) {
                this.box.select("tr").first().remove();
            }
        }

        public void remove1() {
            List<List<Object>> data = (List<List<Object>>) game.get(this.id);
            if (data != null && data.size() > 1) {
                List<Object> t = data.remove(0);
                data.remove(0);
                data.add(0, t);
            }

            if (this.box != null) {
                this.box.select("tr").eq(1).remove();
            }
        }

        public void load() {
            if (game.containsKey(this.id)) {
                List<List<Object>> dict = (List<List<Object>>) game.get(this.id);
                if (fixedKeys != null) {
                    for (String key : fixedKeys) {
                        // Assuming the map stores keys as strings
                        Object value = ((Map<String, Object>) dict.get(0)).get(key);
                        putUI(key, value);
                    }
                } else {
                    for (List<Object> row : dict) {
                        if (columns == 2) {
                            putUI((String) row.get(0), row.get(1));
                        } else {
                            addUI(row.toString()); // Or handle as needed
                        }
                    }
                }
            }
        }

        public String label(int n) {
            return fixedKeys != null ? fixedKeys.get(n) : ((List<List<Object>>) game.get(this.id)).get(n).get(0).toString();
        }
    }


    public static int strToIntDef(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }



    public static void winSpell() {
        int level = Math.min(getI(stats, "WIS") + getI(traits, "Level"), K.Spells.size()); 
        addR(spells, K.Spells.get(randomLow(level)), 1);
    }

    public static String lPick(List<String> list, int goal) {
        String result = pick(list);
        for (int i = 1; i <= 5; ++i) {
            int best = Integer.parseInt(split(result, 1));
            String s = pick(list);
            int b1 = Integer.parseInt(split(s, 1));
            if (Math.abs(goal - best) > Math.abs(goal - b1)) {
                result = s;
            }
        }
        return result;
    }

    public static int abs(int x) {
        return Math.abs(x);
    }
/* winEquip(): Converted to Java. The plus variable is handled more carefully to avoid potential issues. */
    public static void winEquip() {
        int posn = random.nextInt(equips.length()); // Assuming equips has a length() method

        List<String> stuff;
        List<String> better;
        List<String> worse;

        if (posn == 0) {
            stuff = K.Weapons;
            better = K.OffenseAttrib;
            worse = K.OffenseBad;
        } else {
            better = K.DefenseAttrib;
            worse = K.DefenseBad;
            stuff = (posn == 1) ? K.Shields : K.Armors;
        }

        String name = lPick(stuff, getI(traits, "Level"));
        int qual = Integer.parseInt(split(name, 1));
        name = split(name, 0);
        int plus = getI(traits, "Level") - qual;
        if (plus < 0) better = worse;
        int count = 0;
        while (count < 2 && plus != 0) { // Changed plus to plus != 0
            String modifier = pick(better);
            qual = Integer.parseInt(split(modifier, 1));
            modifier = split(modifier, 0);
            if (name.contains(modifier)) break; // no repeats
            if (Math.abs(plus) < Math.abs(qual)) break; // too much
            name = modifier + " " + name;
            plus -= qual;
            ++count;
        }
        if (plus != 0) name = plus + " " + name; // Changed plus to plus != 0
        if (plus > 0) name = "+" + name;

        put(equips, posn, name);
        game.put("bestequip", name); // Changed to game.put
        if (posn > 1) game.put("bestequip", name + " " + equips.label(posn)); // Changed to game.put
    }

    public static int square(int x) {
        return x * x;
    }

    public static void winStat() {
        String i;
        if (odds(1, 2)) {
            i = pick(K.Stats);
        } else {
            int t = 0;
            for (String key : K.PrimeStats) {
                t += square(getI(stats, key));
            }
            t = random.nextInt(t + 1); // Added + 1 to avoid issues with random.nextInt(0)
            for (String key : K.PrimeStats) {
                i = key;
                t -= square(getI(stats, key));
                if (t < 0) break;
            }
        }
        add(stats, i, 1);
    }

    public static String specialItem() {
        return interestingItem() + " of " + pick(K.ItemOfs);
    }

    public static String interestingItem() {
        return pick(K.ItemAttrib) + " " + pick(K.Specials);
    }

    public static String boringItem() {
        return pick(K.BoringItems);
    }



    public static void winItem() {
        if (Math.max(250, random.nextInt(1000)) < inventory.length()) {
            Element row = inventory.rows().get(random.nextInt(inventory.rows().size()));
            if (row.childrenSize() > 0) { // Check if the row has children before accessing them
                add(inventory, row.child(0).text(), 1);
            }
        } else {
            add(inventory, specialItem(), 1);
        }
    }

    public static void completeQuest() {
        questBar.reset(50 + random.nextInt(101));
        if (quests.length() > 0) {
            log("Quest completed: " + game.get("bestquest"));
            quests.checkAll();

            int randomAction = random.nextInt(4);
            switch (randomAction) {
                case 0: winSpell(); break;
                case 1: winEquip(); break;
                case 2: winStat(); break;
                case 3: winItem(); break;
            }
        }

        while (quests.length() > 99) {
            quests.remove0();
        }

        game.put("questmonster", "");

        String caption;
        int randomQuestType = random.nextInt(5);
        switch (randomQuestType) {
            case 0:
                int level = getI(traits, "Level");
                int lev = 0;
                for (int i = 1; i <= 4; ++i) {
                    int montag = random.nextInt(K.Monsters.size());
                    String m = K.Monsters.get(montag);
                    int l = Integer.parseInt(split(m, 1));
                    if (i == 1 || Math.abs(l - level) < Math.abs(lev - level)) {
                        lev = l;
                        game.put("questmonster", m);
                        game.put("questmonsterindex", montag);
                    }
                }
                caption = "Exterminate " + definite(split((String) game.get("questmonster"), 0), 2);
                break;
            case 1:
                caption = "Seek " + definite(interestingItem(), 1);
                break;
            case 2:
                caption = "Deliver this " + boringItem();
                break;
            case 3:
                caption = "Fetch me " + indefinite(boringItem(), 1);
                break;
            case 4:
                int mlev = 0;
                level = getI(traits, "Level");
                for (int ii = 1; ii <= 2; ++ii) {
                    int montag = random.nextInt(K.Monsters.size());
                    String m = K.Monsters.get(montag);
                    int l = Integer.parseInt(split(m, 1));
                    if (ii == 1 || Math.abs(l - level) < Math.abs(mlev - level)) {
                        mlev = l;
                        game.put("questmonster", m);
                    }
                }
                caption = "Placate " + definite(split((String) game.get("questmonster"), 0), 2);
                game.put("questmonster", "");
                break;
        }

        if (!game.containsKey("Quests")) {
            game.put("Quests", new ArrayList<String>());
        }

        List<String> questsList = (List<String>) game.get("Quests");

        while (questsList.size() > 99) {
            questsList.remove(0);
        }

        questsList.add(caption);
        game.put("bestquest", caption);
        quests.addUI(caption);

        log("Commencing quest: " + caption);

        saveGame();
    }

    public static String toRoman(int n) {
        if (n == 0) return "N";
        StringBuilder s = new StringBuilder();

        if (n < 0) {
            s.append("-");
            n = -n;
        }

        while (n >= 10000) {
            n -= 10000;
            s.append("T");
        }
        if (n >= 9000) {
            n -= 9000;
            s.append("MT");
        }
        if (n >= 5000) {
            n -= 5000;
            s.append("A");
        }
        if (n >= 4000) {
            n -= 4000;
            s.append("MA");
        }
        while (n >= 1000) {
            n -= 1000;
            s.append("M");
        }
        if (n >= 900) {
            n -= 900;
            s.append("CM");
        }
        if (n >= 500) {
            n -= 500;
            s.append("D");
        }
        if (n >= 400) {
            n -= 400;
            s.append("CD");
        }
        while (n >= 100) {
            n -= 100;
            s.append("C");
        }
        if (n >= 90) {
            n -= 90;
            s.append("XC");
        }
        if (n >= 50) {
            n -= 50;
            s.append("L");
        }
        if (n >= 40) {
            n -= 40;
            s.append("XL");
        }
        while (n >= 10) {
            n -= 10;
            s.append("X");
        }
        if (n >= 9) {
            n -= 9;
            s.append("IX");
        }
        if (n >= 5) {
            n -= 5;
            s.append("V");
        }
        if (n >= 4) {
            n -= 4;
            s.append("IV");
        }
        while (n >= 1) {
            n -= 1;
            s.append("I");
        }
        return s.toString();
    }


    public static int toArabic(String s) {
        int n = 0;
        s = s.toUpperCase();

        while (s.startsWith("T")) {
            s = s.substring(1);
            n += 10000;
        }
        if (s.startsWith("MT")) {
            s = s.substring(2);
            n += 9000;
        }
        if (s.startsWith("A")) {
            s = s.substring(1);
            n += 5000;
        }
        if (s.startsWith("MA")) {
            s = s.substring(2);
            n += 4000;
        }
        while (s.startsWith("M")) {
            s = s.substring(1);
            n += 1000;
        }
        if (s.startsWith("CM")) {
            s = s.substring(2);
            n += 900;
        }
        if (s.startsWith("D")) {
            s = s.substring(1);
            n += 500;
        }
        if (s.startsWith("CD")) {
            s = s.substring(2);
            n += 400;
        }
        while (s.startsWith("C")) {
            s = s.substring(1);
            n += 100;
        }
        if (s.startsWith("XC")) {
            s = s.substring(2);
            n += 90;
        }
        if (s.startsWith("L")) {
            s = s.substring(1);
            n += 50;
        }
        if (s.startsWith("XL")) {
            s = s.substring(2);
            n += 40;
        }
        while (s.startsWith("X")) {
            s = s.substring(1);
            n += 10;
        }
        if (s.startsWith("IX")) {
            s = s.substring(2);
            n += 9;
        }
        if (s.startsWith("V")) {
            s = s.substring(1);
            n += 5;
        }
        if (s.startsWith("IV")) {
            s = s.substring(2);
            n += 4;
        }
        while (s.startsWith("I")) {
            s = s.substring(1);
            n += 1;
        }
        return n;
    }
 /* completeAct():
game.act is now handled using game.getOrDefault("act", 0) to provide a default value of 0 if it's not yet set.
game.bestplot is now set within the plots.addUI() call.
*/
    public static void completeAct() {
        plots.checkAll();
        game.put("act", (int) game.getOrDefault("act", 0) + 1); // Increment act (default 0)
        int act = (int) game.get("act");
        plotBar.reset(60 * 60 * (1 + 5 * act)); // 1 hr + 5/act
        plots.addUI((String) (game.put("bestplot", "Act " + toRoman(act)))); // Assign and put bestplot

        if (act > 1) {
            winItem();
            winEquip();
        }

        brag('a'); // Implement brag()
    }
/* log():
Uses a Map<Long, String> to store log entries with timestamps.
*/
    public static void log(String line) {
        if (game.containsKey("log")) {
            ((Map<Long, String>) game.get("log")).put(System.currentTimeMillis(), line);
        }
        // TODO: Logging mechanism (e.g., write to file, display in UI)
    }
/* task():
game.kill is now handled correctly.
Assumes Kill is a UI element (like a TextView in Android or a Label in JavaFX) that has a text() method.
*/
    public static void task(String caption, int msec) {
        game.put("kill", caption + "...");
        if (kill != null) { // Assuming 'kill' is an Element
            kill.text((String) game.get("kill"));
        }
        log((String) game.get("kill"));
        taskBar.reset(msec);
    }

    public static void add(MyList list, String key, int value) {
        int current = getI(list, key);
        put(list, key, current + value);

		if (LOGGING) {
			if (value != 0) {
				String line = (value > 0) ? "Gained" : "Lost";
				if (key.equals("Gold")) {
					key = "gold piece";
					line = (value > 0) ? "Got paid" : "Spent";
				}
				if (value < 0) value = -value;
				line = line + " " + indefinite(key, value);
				log(line);
			}
		}
    }

    public static void addR(MyList list, String key, String value) {
        put(list, key, toRoman(toArabic(value) + toArabic(get(list, key))));
    }

    public static String get(MyList list, String key) {
        if (list.fixedKeys != null) {
            if (key instanceof Integer) {
                key = list.fixedKeys.get((Integer) key);
            }
            return (String) ((Map<String, Object>) game.get(list.id)).get(key);
        } else if (key instanceof Integer) {
            List<List<Object>> data = (List<List<Object>>) game.get(list.id);
            if ((Integer) key < data.size()) {
                return data.get((Integer) key).get(1).toString();
            } else {
                return "";
            }
        } else {
            List<List<Object>> data = (List<List<Object>>) game.get(list.id);

            for (List<Object> row : data) {
                if (row.get(0).equals(key)) {
                    return row.get(1).toString();
                }
            }
            return "";
        }
    }

    public static int getI(MyList list, String key) {
        try {
            return Integer.parseInt(get(list, key));
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static void levelUp() {
        add(traits, "Level", 1);
        add(stats, "HP Max", getI(stats, "CON") / 3 + 1 + random.nextInt(4));
        add(stats, "MP Max", getI(stats, "INT") / 3 + 1 + random.nextInt(4));
        winStat();
        winStat();
        winSpell();
        expBar.reset(levelUpTime(getI(traits, "Level"))); // Implement levelUpTime()
        brag('l');
    }

    public static void clearAllSelections() {
        for (MyList list : allLists) { // Assuming AllLists is a Collection of MyList
            list.clearSelection();
        }
    }

    public static String roughTime(int s) {
        if      (s < 120) return s + " seconds";
        else if (s < 60 * 120) return s / 60 + " minutes";
        else if (s < 60 * 60 * 48) return s / 3600 + " hours";
        else if (s < 60 * 60 * 24 * 60) return s / (3600 * 24) + " days";
        else if (s < 60 * 60 * 24 * 30 * 24) return s / (3600 * 24 * 30) + " months";
        else return s / (3600 * 24 * 30 * 12) + " years";
    }

    public static int pos(String needle, String haystack) {
        return haystack.indexOf(needle) + 1;
    }

    private static boolean dealing = false; // You might need to use this later

    public static void timer1Timer() {
        if (taskBar.done()) {
            game.put("tasks", (int) game.getOrDefault("tasks", 0) + 1);
            game.put("elapsed", (double) game.getOrDefault("elapsed", 0.0) + taskBar.max() / 1000.0);

            clearAllSelections();

            if (game.getOrDefault("kill", "").equals("Loading....")) {
                taskBar.reset(0);
            }

            boolean gain = pos("kill|", (String) game.getOrDefault("task", "")) == 1;

            if (gain) {
                if (expBar.done()) {
                    levelUp();
                } else {
                    expBar.increment(taskBar.max() / 1000.0);
                }
            }

            if (gain && (int) game.getOrDefault("act", 0) >= 1) {
                if (questBar.done() || quests.length() == 0) {
                    completeQuest();
                } else {
                    questBar.increment(taskBar.max() / 1000.0);
                }
            }

            if (gain || (int) game.getOrDefault("act", 0) == 0) {
                if (plotBar.done()) {
                    interplotCinematic();
                } else {
                    plotBar.increment(taskBar.max() / 1000.0);
                }
            }

            dequeue();
        } else {
            long elapsed = timeGetTime() - clock.lasttick;
            if (elapsed > 100) elapsed = 100;
            if (elapsed < 0) elapsed = 0;
            taskBar.increment((int) elapsed);
        }

        startTimer();
    }

/* 
formCreate():
AllBars and AllLists are now initialized using Arrays.asList().
*/
    public static void formCreate() {
        expBar = new ProgressBar("ExpBar", "$remaining XP needed for next level");
        encumBar = new ProgressBar("EncumBar", "$position/$max cubits");
        plotBar = new ProgressBar("PlotBar", "$time remaining");
        questBar = new ProgressBar("QuestBar", "$percent% complete");
        taskBar = new ProgressBar("TaskBar", "$percent%");

        allBars = Arrays.asList(expBar, plotBar, taskBar, questBar, encumBar); // Use Arrays.asList

        traits = new MyList("Traits", 2, K.Traits);
        stats = new MyList("Stats", 2, K.Stats);
        spells = new MyList("Spells", 2, null); // fixedKeys is null
        equips = new MyList("Equips", 2, K.Equips);
        inventory = new MyList("Inventory", 2, null); // fixedKeys is null
        plots = new MyList("Plots", 1, null); // fixedKeys is null
        quests = new MyList("Quests", 1, null); // fixedKeys is null

        plots.load = (sheet) -> {
            int start = Math.max(0, (int) game.getOrDefault("act", 0) - 99);
            int end = (int) game.getOrDefault("act", 0);
            for (int i = start; i <= end; ++i) {
                plots.addUI(i == 0 ? "Prologue" : "Act " + toRoman(i));
            }
            return null;
        };

        allLists = Arrays.asList(traits, stats, spells, equips, inventory, plots, quests);

        // ... (Equivalent of $(document).ready() and other jQuery/DOM related code)
        // Note: You'll need to replace jQuery selectors and event handlers with
        // their Java/JavaFX equivalents if you're using a UI framework.

        // Example (replace with your actual UI code):
        // kill = document.getElementById("Kill"); // Or similar

        // ... (Rest of FormCreate logic, including storage.loadSheet and prepPopup)
    }


    public static void prepPopup() {
        // This function is primarily for UI manipulation (resizing, moving the window),
        // which is highly platform-specific (browser vs. other environments).  Jsoup
        // is primarily for HTML parsing, not UI interactions.  Therefore, the direct
        // translation to pure Java is not possible without a UI framework (like
        // JavaFX, Swing, or Android).

        // The JavaScript code you provided interacts with the browser window directly.
        // Here's a breakdown of what it does and how you might approach it in Java
        // with a UI framework:

        // 1. `document.body.classList.add("bare");`
        //    - Adds the class "bare" to the document's body.  In a Java UI framework,
        //      you would set a style or CSS class on the main container/window of your UI.

        // 2. `window.resizeBy(...)`
        //    - Resizes the browser window.  In Java UI, you'd use methods like
        //      `Stage.setWidth()` and `Stage.setHeight()` (JavaFX) or similar methods
        //      in other frameworks.  The `offsetWidth` and `offsetHeight` would need
        //      to be obtained from your UI elements.

        // 3. Mouse event handling:
        //    - The code sets up mouse event listeners (mousedown, mouseup, mousemove)
        //      to enable window dragging.  In Java UI, you would use event listeners
        //      provided by the UI framework (e.g., `setOnMousePressed()`,
        //      `setOnMouseReleased()`, `setOnMouseDragged()` in JavaFX).

        // Example (JavaFX - adapt to your specific UI):

        /*
        // Assuming 'stage' is your JavaFX Stage (window) and 'mainContainer' is the root UI element.
        stage.getScene().getRoot().getStyleClass().add("bare"); // Equivalent of adding a CSS class

        // Get sizes from your UI elements (replace with your actual element references).
        double width = mainContainer.getWidth(); // Example
        double height = mainContainer.getHeight(); // Example

        stage.setWidth(width);
        stage.setHeight(height);

        // Mouse event handling (JavaFX example)
        titlebar.setOnMousePressed(e -> {
            delta = new Point2D(e.getSceneX(), e.getSceneY());
        });

        stage.setOnMouseDragged(e -> {
            if (delta != null) {
                stage.setX(e.getScreenX() - delta.getX());
                stage.setY(e.getScreenY() - delta.getY());
            }
        });
        */

        // The above is a general example.  The specifics will depend entirely on
        // the Java UI framework you choose (JavaFX, Swing, etc.).  If you are not
        // using a UI framework (e.g., this is a server-side application), then
        // this `prepPopup()` function would not be relevant.
    }


    public static void pause(int msec) {
        // The JavaScript pause() function uses showModalDialog, which is a browser-specific
        // method for creating a modal dialog. This is not directly translatable to
        // standard Java.  If you are using a UI framework (like JavaFX), you'll need
        // to use the appropriate methods for pausing execution and updating the UI.

        // Here are two main approaches, depending on your context:

        // 1. If you are in a Java UI thread (e.g., in a JavaFX event handler):
        //    You should *never* use Thread.sleep() in the UI thread because it will
        //    freeze the UI. Instead, use Platform.runLater() to schedule the UI
        //    update after the pause.

        // 2. If you are in a background thread:
        //    You can use Thread.sleep() or a CountDownLatch.

        // Option 1: In a JavaFX UI thread (example):
        
        Platform.runLater(() -> {
            // Update UI (if needed) before the pause
        });

        // Schedule the action to occur after the pause.
        PauseTransition pause = new PauseTransition(Duration.millis(msec));
        pause.setOnFinished(event -> {
            Platform.runLater(() -> {
                // Continue execution after the pause
                // Update the UI again if necessary.
            });
        });
        pause.play();
    }    	

    public static void quit() {
        // The JavaScript code unbinds the 'unload' event, saves the game, and then
        // either closes the window (if it's a popup) or redirects to "roster.html".

        // Here's how you might approach this in Java, keeping in mind that the specifics
        // depend on your environment (e.g., whether you have a UI framework like JavaFX):

        // 1. Unbinding 'unload' event:
        //    - If you're using a UI framework, you would typically remove the event
        //      listener that handles the window's close/unload event.  The way you do
        //      this depends entirely on the framework.

        // 2. Saving the game:
        //    - The JavaScript code uses a callback function after saving.  In Java,
        //      you can use a `CompletableFuture` to handle the asynchronous save
        //      operation and then proceed with closing or redirecting.

        // 3. Closing or redirecting:
        //    - If you're in a browser environment (e.g., using a WebView in JavaFX),
        //      you can use JavaScript execution to interact with the browser window.
        //    - If you're in a desktop application (e.g., JavaFX or Swing), you would use
        //      the appropriate methods for closing the window or navigating within
        //      your application.

        // Example (JavaFX with WebView - illustrative):

        /*
        // Assuming 'webView' is your JavaFX WebView and 'stage' is your Stage.

        // 1. Remove unload event listener (JavaFX specific - adapt as needed): */
        stage.setOnCloseRequest(event -> {
            // Do nothing (or handle differently) when the window is closed.
            // The quit() method will handle the closing.
        });

        // 2. Save the game (using CompletableFuture):
        CompletableFuture<Void> saveFuture = saveGame(); // Assuming saveGame() returns a CompletableFuture<Void>

        saveFuture.thenRun(() -> {
            // 3. Close or redirect (in JavaFX WebView context):
            if (isPopup()) { // Implement isPopup() to check if it's a popup
                Platform.runLater(() -> { // Update UI from JavaFX Application Thread
                    webView.getEngine().executeScript("window.close();"); // Close the WebView (browser window)
                });
            } else {
                Platform.runLater(() -> { // Update UI from JavaFX Application Thread
                    webView.getEngine().load("roster.html"); // Navigate within the WebView
                });
            }
        });

        // ... handle exceptions if the save operation fails
        saveFuture.exceptionally(ex -> {
            // Handle the exception
            return null;
        });
        

        // Example (JavaFX without WebView - illustrative):

        /*
        // 1. Remove unload event listener (as shown above)

        // 2. Save the game (using CompletableFuture - as shown above) */

        saveFuture.thenRun(() -> {
            // 3. Close or redirect (in JavaFX desktop context):
            if (isPopup()) {
                Platform.runLater(() -> { // JavaFX Application Thread
                    stage.close(); // Close the JavaFX window
                });
            } else {
                // Navigation within your JavaFX application (replace with your navigation logic):
                Platform.runLater(() -> { // JavaFX Application Thread
                    // ... your navigation code (e.g., change scene, load a new FXML file, etc.)
                });
            }
        });

    }


    public static void hotOrNot() {
        // Figure out which spell is best
        if (spells.length() > 0) { // Use spells.length() instead of Spells.length()
            final int flat = 1; // Flattening constant
/* Spells Sorting:
The most significant change is how the "best spell" is determined. 
The JavaScript code uses a somewhat unusual way of comparing spells based on both their value and their index.
The Java code now uses a List<Map.Entry<String, Object>> to store spell names and values, and then uses Collections.sort() with a custom Comparator to implement the same logic. This is a more standard and readable way to sort in Java.
*/
            // Java equivalent of the JavaScript sorting logic
            List<Map.Entry<String, Object>> spellEntries = new ArrayList<>();
            for (int i = 0; i < spells.length(); i++) {
                spellEntries.add(new AbstractMap.SimpleEntry<>(spells.label(i), get(spells, i)));
            }

            Collections.sort(spellEntries, new Comparator<Map.Entry<String, Object>>() {
                @Override
                public int compare(Map.Entry<String, Object> a, Map.Entry<String, Object> b) {
                    int aValue = toArabic(a.getValue().toString());
                    int bValue = toArabic(b.getValue().toString());
                    return Double.compare((double) (spellEntries.indexOf(a) + flat) * aValue,
                            (double) (spellEntries.indexOf(b) + flat) * bValue);
                }
            });

            if (!spellEntries.isEmpty()) {
                Map.Entry<String, Object> bestSpellEntry = spellEntries.get(spellEntries.size() - 1);
                game.put("bestspell", bestSpellEntry.getKey() + " " + bestSpellEntry.getValue());
            } else {
                game.put("bestspell", "");
            }

        } else {
            game.put("bestspell", "");
        }

        // And which stat is best?
        int best = 0;
        for (int i = 1; i <= 5; ++i) {
            if (getI(stats, stats.label(i)).compareTo(getI(stats, stats.label(best))) > 0) {
                best = i;
            }
        }
        game.put("beststat", stats.label(best) + " " + getI(stats, stats.label(best)));
    }

/* CompletableFuture:
The saveGame() method now returns a CompletableFuture<Void>.
This is crucial for handling asynchronous save operations.
The addToRoster method of the Storage class is also assumed to return a CompletableFuture<Void>.
*/
    public CompletableFuture<Void> saveGame() {
        log("Saving game: " + gameSaveName());
        hotOrNot();
        game.put("date", new Date().toString()); // Use Java's Date class
        game.put("stamp", System.currentTimeMillis()); // Current time in milliseconds
        game.put("seed", randSeed()); // Implement randSeed()

        // Use a CompletableFuture to handle the asynchronous save operation
        return storage.addToRoster(game)
              .thenRun(() -> {
                    // Optional: Code to run after successful save (if needed)
                })
              .exceptionally(ex -> {
                    // Handle exceptions during the save operation
                    log("Error saving game: " + ex.getMessage());
                    return null; // Or throw an exception if you want to propagate it
                });
    }

	


    public static void loadGame(Map<String, Object> sheet) {
        if (sheet == null || sheet.isEmpty()) {
            System.err.println("Error loading game"); // Or use a proper logging mechanism
            // Handle the error appropriately (e.g., redirect, display a message)
            return;
        }

        game = sheet;
        
        Platform.runLater(() -> { // Update UI on the JavaFX Application Thread
            titleLabel.setText("Progress Quest - " + gameSaveName()); // Set title label text
            if (isIOS()) { // Implement isIOS()
                titleLabel.setText(gameSaveName());
            }
            primaryStage.setTitle(titleLabel.getText()); // Set window title
        });
        

        randSeed((String) game.get("seed")); // Assuming seed is a String
        for (Object element : allBars) { // Assuming allBars is a List<Object> or a similar collection
            if (element instanceof Loadable) { // Check if the element is Loadable
                ((Loadable) element).load(game); // Call the load method
            }
        }
        for (Object element : allLists) { // Assuming allLists is a List<Object> or a similar collection
            if (element instanceof Loadable) { // Check if the element is Loadable
                ((Loadable) element).load(game); // Call the load method
            }
        }

        if (kill != null) { // Assuming kill is a UI element (replace with your actual element)
            kill.text((String) game.get("kill"));
        }

        clearAllSelections();

        for (Object list : Arrays.asList(plots, quests)) { // Assuming plots and quests are instances of a class that can be checked.
            if (list instanceof Checkable) {
                ((Checkable) list).checkAll(true);
            }
        }

        patch("Innoculate", "Inoculate");
        patch("Tonsilectomy", "Tonsillectomy");

        log("Loaded game: " + game.get("Traits.Name")); // Assuming Traits.Name is a String
        if (!game.containsKey("elapsed")) {
            brag('s');
        }

        startTimer();
    }
	
	
    public static String gameSaveName() {
        if (!game.containsKey("saveName")) {  // Check if saveName exists as a key
            String name = get(traits, "Name"); // Assuming get(traits, "Name") returns a String
            String saveName = name; // Initialize saveName with the player's name

            if (game.containsKey("online")) {
                Map<String, Object> online = (Map<String, Object>) game.get("online"); // Cast online
                if (online != null && online.containsKey("realm")) { // Check if online and realm exist
                    saveName += " [" + online.get("realm") + "]";
                }
            }
            game.put("saveName", saveName);
        }
        return (String) game.get("saveName"); // Cast saveName when returning it
    }
	
    public static String inputBox(String message, String def) {
        // The JavaScript prompt() function creates a simple dialog box that prompts the
        // user for input.  In Java, you can achieve similar functionality using UI
        // elements from a UI framework like JavaFX or Swing.

        // Here's how you can do it using JavaFX:

        TextInputDialog dialog = new TextInputDialog(def == null ? "" : def); // Use TextInputDialog
        dialog.setTitle("Input"); // Set the dialog title
        dialog.setHeaderText(message); // Set the header text (prompt message)

        Optional<String> result = dialog.showAndWait(); // Show the dialog and wait for input

        if (result.isPresent()) {
            return result.get(); // Return the entered text
        } else {
            return null; // Or return an empty string "" if the user cancels
        }


        // If you are using a different UI framework (Swing, Android, etc.), you'll need
        // to use the equivalent methods provided by that framework for creating input
        // dialogs.  The general principle is the same: create a dialog, prompt for
        // input, and return the result.

        // If you are not using a UI framework (e.g., a server-side application), then
        // this function might not be relevant.  You might read input from the
        // console or use other means.

        // Example using console input (if no UI framework):
        /*
        System.out.print(message + ": ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
        */
    }


	
// Pascal/Delphi compilation directive replacement
private static final boolean LOGGING = "true".equalsIgnoreCase(System.getProperty("PQLOGGING"));

/* main() method:
A simple main method is provided to demonstrate how to start and stop the timer and use the queue.
*/
    public static void main(String[] args) {
        startTimer(); // Start the timer
        q("Task 1");
        q("Task 2");

        try {
            Thread.sleep(5000); // Let the timer run for a bit
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        stopTimer(); // Stop the timer
    }








}