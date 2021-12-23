import java.util.PriorityQueue;

/**
 * Accomplished using the EduTools plugin by JetBrains https://plugins.jetbrains.com/plugin/10081-edutools
 *
 * To modify the template, go to Preferences -> Editor -> File and Code Templates -> Other
 */

public class Day23 {

   public static long solution;
   public static final int numBeds = 4;

   public static void main(String[] args) {
      //String rooms = "BACDBCDA";
      //String rooms = "DCDCABAB";

      // Part II
      //String rooms = "BDDACCBDBBACDACA";
      String rooms = "DDDCDCBCABABAACB";

      String hallway = "..?.?.?.?..";
      //solution = tope + 100;
      solution = Integer.MAX_VALUE;
      //System.out.println(discardCorrectRooms(".X.DC.DA"));
      //System.out.println(discardCorrectRooms("BAAABBCBCCCDDDDD"));

      /**
      *    1. Calculate all options so far
       *   2. Choose the best one
       *   3. Move to that option recursively
      * */
      getCost(rooms, hallway, 0);
      System.out.println(solution);
   }

   public static class Option implements Comparable<Option> {
      String rooms;
      String hallway;
      long cost;

      public Option(String rooms, String hallway, long cost) {
         this.rooms = rooms;
         this.hallway = hallway;
         this.cost = cost;
      }

      @Override
      public int compareTo(Option o) {
         return Long.compare(this.cost, o.cost);
      }

      @Override
      public String toString() {
         return "Option{" +
                        "rooms='" + rooms + '\'' +
                        ", hallway='" + hallway + '\'' +
                        ", cost=" + cost +
                        "}\n";
      }
   }

   public static final int[] energy = {1,10,100,1000};

   private static void getCost(String rooms, String hallway, long cost) {
      rooms = discardCorrectRooms(rooms);
      if (rooms.equals("XXXXXXXXXXXXXXXX")) {
         if (cost < solution) {
            System.out.println("New cost " + cost);
            solution = cost;
         }
         return;
      }
      PriorityQueue<Option> pq = new PriorityQueue<>();
      for (int level = 0; level < numBeds; level++) {
         fillOptAnyToHallWay(pq, rooms, hallway, cost, level);
      }
      fillOptHallWayToRooms(pq, rooms, hallway, cost);

      while (!pq.isEmpty()) {
         Option cur = pq.poll();
         //System.out.print(cost + ": Taking " + cur);
         if (cur.cost >= solution) continue;
         getCost(cur.rooms, cur.hallway, cur.cost);
      }

      //System.out.println("No more moves");
   }

   private static void fillOptHallWayToRooms(PriorityQueue<Option> pq, String rooms, String hallway, long cost) {
      // Hallway to Room
      for (int i = 0; i < hallway.length(); ++i) {
         char letter = hallway.charAt(i);
         if (letter == '?' || letter == '.') continue;
         int rightRoom = numBeds * (letter - 'A' + 1) - 1;
         int door = (rightRoom + 1) / 2;
         // Skip if the destination is not suitable
         boolean stop = false;
         for (int b = 0; b < numBeds; ++b) {
            if (rooms.charAt(rightRoom - b) != '.' && rooms.charAt(rightRoom - b) != 'X') stop = true;
         }
         if (stop) continue;
         // Find direction left or right
         //System.out.println("From " + i + " to " + door);
         int dir = door >= i ? 1 : -1;
         //System.out.println(dir);
         // Iterate while dot or question mark
         boolean validPath = true;
         int distance = 1;
         for (int x = i + dir; x >= 0 && x < hallway.length() && x != door && validPath; x += dir) {
            if (hallway.charAt(x) == '.' || hallway.charAt(x) == '?') {
               distance++;
            } else {
               validPath = false;
            }
         }
         //System.out.println("Dist: " + distance);
         // Sum to the distance 1 to 4
         if (validPath) {
            String moveRoom = "";
            for (int b = 0; b < numBeds; b++) {
               if (rooms.charAt(rightRoom - b) == '.') {
                  distance += numBeds - b;
                  moveRoom = rooms.substring(0,rightRoom - b) + letter + rooms.substring(rightRoom-b+1);
                  break;
               }
            }
            String moveHallway = hallway.substring(0,i) + "." + hallway.substring(i+1);
            pq.add(new Option(moveRoom, moveHallway, cost + distance * energy[letter - 'A']));
         }
      }
   }

   private static void fillOptAnyToHallWay(PriorityQueue<Option> pq, String rooms, String hallway, long cost, int level) {
      for (int i = level, h = 2; i < (numBeds * 4); i+=numBeds, h+=2) {
         char letter = rooms.charAt(i);
         if (letter == 'X' || letter == '.') continue;
         int first = numBeds * (int)(i/numBeds);
         boolean skip = false;
         for (int dependency = i - 1; dependency >= first; dependency--) {
            if (rooms.charAt(dependency) != '.') {
               skip = true;
               break;
            }
         }
         if (skip) continue;
         // Left hallway
         String moveRoom = rooms.substring(0,i) + "." + rooms.substring(i+1);
         int moveCost = level + 1;
         for (int cand = h; cand >= 0 && (hallway.charAt(cand) == '.' || hallway.charAt(cand) == '?'); --cand, moveCost++) {
            if (hallway.charAt(cand) == '?') continue; // door
            String moveHallway = hallway.substring(0,cand) + letter + hallway.substring(cand+1);
            Option opt = new Option(moveRoom, moveHallway, cost + moveCost * energy[letter - 'A']);
            pq.add(opt);
         }
         // Right hallway
         moveCost = level + 1;
         for (int cand = h; cand < hallway.length() && (hallway.charAt(cand) == '.' || hallway.charAt(cand) == '?'); ++cand, moveCost++) {
            if (hallway.charAt(cand) == '?') continue; // door
            String moveHallway = hallway.substring(0,cand) + letter + hallway.substring(cand+1);
            Option opt = new Option(moveRoom, moveHallway, cost + moveCost * energy[letter - 'A']);
            pq.add(opt);
         }
      }
   }

   private static String discardCorrectRooms(String rooms) {
      StringBuilder newRooms = new StringBuilder(rooms);
      String pattern = "AAAABBBBCCCCDDDD";
      for (int i = numBeds-1; i < numBeds * 4; i += numBeds) {
         int p = 0;
         while (p < numBeds && (newRooms.charAt(i-p) == pattern.charAt(i-p) || newRooms.charAt(i-p) == 'X')) {
            newRooms.setCharAt(i-p, 'X');
            p++;
         }
      }
      return newRooms.toString();
   }
}
