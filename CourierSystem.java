/******************************************************************************

                            Online Java Compiler.
                Code, Compile, Run and Debug java program online.
Write your code in this editor and press "Run" button to execute it.

*******************************************************************************/

import java.util.Scanner;

public class CourierSystem {
    // W6: A distance matrix (stop x stop, 2-D)
    // Index 0 is the Depot. Indices 1-4 are stops S1-S4.
    static final String[] STOPS = {"Depot", "S1", "S2", "S3", "S4"};
    static final double[][] DISTANCES = {
        {0.0, 5.2, 8.1, 4.5, 9.0},  // Depot (0)
        {5.2, 0.0, 3.4, 6.1, 2.2},  // S1 (1)
        {8.1, 3.4, 0.0, 7.3, 4.1},  // S2 (2)
        {4.5, 6.1, 7.3, 0.0, 5.5},  // S3 (3)
        {9.0, 2.2, 4.1, 5.5, 0.0}   // S4 (4)
    };

    // Parallel arrays to hold parcel data (before OOP encapsulation in W7)
    static final int MAX_PARCELS = 100;
    static int parcelCount = 0;
        
    static String[] parcelIds = new String[MAX_PARCELS];
    static int[] parcelDestinations = new int[MAX_PARCELS]; // stores stop index 1-4
        
    // W2: double weights and distances, int stop counts, boolean delivered
    static double[] parcelWeights = new double[MAX_PARCELS];     
    static String[] parcelZones = new String[MAX_PARCELS];
    static int[] parcelStatus = new int[MAX_PARCELS];     
    static boolean[] isDelivered = new boolean[MAX_PARCELS];

    // Status Constants
    static final int REGISTERED = 0;
    static final int OUT_FOR_DELIVERY = 1;
    static final int DELIVERED = 2;
    static final int FAILED = 3;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        // W1: Console dispatch and tracking menu loop
        while (running) {
            System.out.println("\n=== Courier Depot ===");
            System.out.println("1) Add parcel  2) Build routes  3) Track  4) Report  5) Exit");
            System.out.print("Choice: ");
                        
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            // W5: Methods per operation
            if (choice == 1) {
                addParcel(scanner);
            } else if (choice == 2) {
                buildRoutes(scanner);
            } else if (choice == 3) {
                trackParcel(scanner);
            } else if (choice == 4) {
                generateReport();
            } else if (choice == 5) {
                running = false;
                System.out.println("Exiting system...");
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    // W5: Method for adding a parcel
    static void addParcel(Scanner scanner) {
        if (parcelCount >= MAX_PARCELS) {
            System.out.println("Error: Depot capacity exceeded!");
            return;
        }
        System.out.print("Enter Parcel ID (e.g., P101): ");
        String id = scanner.nextLine();
                
        System.out.print("Enter Destination Stop (1 for S1, 2 for S2, 3 for S3, 4 for S4): ");
        int destIndex = scanner.nextInt();
                
        System.out.print("Enter Weight (kg): ");
        double weight = scanner.nextDouble();
        scanner.nextLine(); // consume newline
                
        System.out.print("Enter Zone (e.g., North, South): ");
        String zone = scanner.nextLine();
        
        // Registering data into parallel arrays
        parcelIds[parcelCount] = id;
        parcelDestinations[parcelCount] = destIndex;
        parcelWeights[parcelCount] = weight;
        parcelZones[parcelCount] = zone;
        parcelStatus[parcelCount] = REGISTERED;
        isDelivered[parcelCount] = false;
        parcelCount++;
        System.out.println("Parcel " + id + " REGISTERED.");
    }

    // W5: Method for building routes
    static void buildRoutes(Scanner scanner) {
        System.out.print("Enter Zone to build route for: ");
        String targetZone = scanner.nextLine();
                
        System.out.print("Enter truck capacity (kg): ");
        double capacity = scanner.nextDouble();
        double currentLoad = 0.0;
        int[] routeStops = new int[STOPS.length];
        int stopCount = 0;
                
        // Track which stops need to be visited for this route
        boolean[] stopsToVisit = new boolean[STOPS.length];
        
        // W4: Loops to build routes
        for (int i = 0; i < parcelCount; i++) {
            // W3: Branch on capacity, zone, and status transitions
            if (parcelZones[i].equalsIgnoreCase(targetZone) && parcelStatus[i] == REGISTERED) {
                if (currentLoad + parcelWeights[i] <= capacity) {
                    currentLoad += parcelWeights[i];
                    parcelStatus[i] = OUT_FOR_DELIVERY; // Valid state transition
                    int dest = parcelDestinations[i];
                    
                    if (!stopsToVisit[dest]) {
                        stopsToVisit[dest] = true;
                        stopCount++;
                    }
                    System.out.println("Loaded " + parcelIds[i] + " onto route.");
                } else {
                    System.out.println("Capacity exceeded. Parcel " + parcelIds[i] + " held back.");
                }
            }
        }
        if (stopCount == 0) {
            System.out.println("No parcels to route for " + targetZone + ".");
            return;
        }
        
        // Initialize ordering structures
        int[] orderedRoute = new int[stopCount + 1]; // +1 to include Depot at the start
        orderedRoute[0] = 0; // Start at Depot
                
        // W5: Recursion in nearest-neighbour ordering
        recursiveNearestNeighbour(0, stopsToVisit, orderedRoute, 1, stopCount);
        
        // Display the ordered route and calculate total distance
        System.out.print("Route generated: Depot");
        double totalDistance = 0.0;
        int currentStop = 0;
        for (int i = 1; i <= stopCount; i++) {
            int nextStop = orderedRoute[i];
            System.out.print(" -> " + STOPS[nextStop]);
            totalDistance += DISTANCES[currentStop][nextStop];
            currentStop = nextStop;
        }
        System.out.println(" (dist " + totalDistance + " km)");
    }

    // W5: Recursive Nearest-Neighbour logic
    static void recursiveNearestNeighbour(int currentStopIndex, boolean[] unvisited, int[] routeArr, int step, int totalStopsToVisit) {
        // Base case: If we've ordered all required stops, exit recursion
        if (step > totalStopsToVisit) {
            return; 
        }
        int nextNearestStop = -1;
        double minDistance = Double.MAX_VALUE;
        
        // W4: Loop to find the closest unvisited stop
        for (int i = 1; i < STOPS.length; i++) {
            if (unvisited[i]) {
                double dist = DISTANCES[currentStopIndex][i];
                if (dist < minDistance) {
                    minDistance = dist;
                    nextNearestStop = i;
                }
            }
        }
        if (nextNearestStop != -1) {
            unvisited[nextNearestStop] = false; // Mark as visited
            routeArr[step] = nextNearestStop; // Add to route
                        
            // Recursive call for the next step
            recursiveNearestNeighbour(nextNearestStop, unvisited, routeArr, step + 1, totalStopsToVisit);
        }
    }

    // W5: Method for tracking
    static void trackParcel(Scanner scanner) {
        System.out.print("Enter Parcel ID to update/track: ");
        String id = scanner.nextLine();
                
        for (int i = 0; i < parcelCount; i++) {
            if (parcelIds[i].equalsIgnoreCase(id)) {
                System.out.println("Current Status: " + getStatusString(parcelStatus[i]));
                                
                // W3: Branch on status transitions
                if (parcelStatus[i] == OUT_FOR_DELIVERY) {
                    System.out.print("Mark as (1) DELIVERED or (2) FAILED? ");
                    int update = scanner.nextInt();
                    if (update == 1) {
                        parcelStatus[i] = DELIVERED;
                        isDelivered[i] = true;
                        System.out.println(id + " -> DELIVERED");
                    } else if (update == 2) {
                        parcelStatus[i] = FAILED;
                        System.out.println(id + " -> FAILED (queued for tomorrow)");
                    } else {
                        System.out.println("Invalid transition.");
                    }
                } else {
                    System.out.println("Cannot update status from " + getStatusString(parcelStatus[i]));
                }
                return;
            }
        }
        System.out.println("Parcel not found.");
    }

    static void generateReport() {
        int deliveredCount = 0;
        int failedCount = 0;
                
        for (int i = 0; i < parcelCount; i++) {
            if (parcelStatus[i] == DELIVERED) deliveredCount++;
            if (parcelStatus[i] == FAILED) failedCount++;
        }
                
        System.out.println("=== Daily Report ===");
        System.out.println("Total Registered: " + parcelCount);
        System.out.println("Delivered Today: " + deliveredCount);
        System.out.println("Failed Today: " + failedCount + " (queued for reattempt)");
    }

    // Helper to translate int status to String
    static String getStatusString(int status) {
        if (status == REGISTERED) return "REGISTERED";
        if (status == OUT_FOR_DELIVERY) return "OUT_FOR_DELIVERY";
        if (status == DELIVERED) return "DELIVERED";
        if (status == FAILED) return "FAILED";
        return "UNKNOWN";
    }
}