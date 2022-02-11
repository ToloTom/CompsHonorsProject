import java.util.*;

public class MagicSquare {
    public static void main(String[] args){
        // For analysis, we want to keep track of the time it takes to finish this algorithm.
        final long startTime = System.currentTimeMillis();
        
        // This is the method that will produce all magic squares given an input for the desired size of magic squares.
        // Note that this is currently only working for n =2, n = 3 and n = 4 
        constructMagicSquares(4);

        // After the algorithm has finished, keep track of the time it took to finish.
        final long elapsedTimeMillis = System.currentTimeMillis() - startTime;
        
        // Inform the user of how long the total process took. 
        System.out.println("Total time to finish in milliseconds: " + elapsedTimeMillis);
    }

    public static void constructMagicSquares(int n){

        // Calculate the magic value.
        int magicValue = magicSum(n); 

        // Construct the list of numbers 1, 2, ..., n^2.
        ArrayList<Integer> listOfInts = numbers(n); 

        // Using the listOfInts and the size of the magic square, 
        // find all unique vectors whose elements sum to the magic sum. 
        List<Set<Integer>> listOfVectors = SubsetSum.subsetSum(listOfInts, n, magicValue); 
        
        // At this point we have all the unique vectors of size n that sum to the magic value (call them magic vectors). 

        // The next step is to determine all possible combinations (order does not matter) of these magic vectors such that
        // each combination contains n-many disjoint vectors. 

        // Note that this is the portion of the algorithm with the greatest time complexity.  
         List<List<Set<Integer>>> listOfDisjointGroupings = RowVectorCombinations.rowVectorCombination(listOfVectors, n, listOfInts);
        
        // At this point we have a set of sets of vectors (call them magic combinations) in which each magic combination
        // is constructed from n-many disjoint magic vectors.

        //ie: [[1, 5, 9], [2, 6, 7], [3, 4, 8]] forms one such magic combination for n = 3.

        // Note that we may set these magic combinations to either be the rows or columns of any magic square 
        // since there are n many disjoint magic vectors in each magic combination. 

        // Furthermore note that there are some magic combinations that can be combined to form a possible 
        // row and column combination.  
        
        //ie: [[[1, 5, 9], [2, 6, 7], [3, 4, 8]], [[1, 6, 8], [2, 4, 9], [3, 5, 7]]]

        // That is, we can set the first magic combination to populate the rows and the second magic combination to populate the columns,
        // and vice versa. 

        // Group theory portion of the algorithm will explain why, again, order does not matter here. 

        // Thus, our next goal is to determine all possible row and column combinations from the set of magic combinations
        List<List<List<Set<Integer>>>> listOfRowColGroupings = RowColumnCombinations.rowColumnCombinations(listOfDisjointGroupings); 

        // With the set of row and column combinations one could construct a brute force method to apply all possible permutations (order matters)
        // to determine all the magic squares of size n. However, I believe this kind of approach misses the opportunity to eliminate a lot of redundancy. 

        // That is, we can take this set of row and column combinations and find their respective diagonal vectors that fit within the 
        // context of the determined rows and columns. 
        
        List<List<List<Set<Integer>>>> listOfRowColDiagGroupings = findDiagonals(listOfRowColGroupings, listOfVectors);

        // Interestingly, some row and column combinations have less than two possible diagonal elements, which implies, by definition,  
        // that no such magic square can be constructed from the row and column combination.  
        
        // Since we know that a magic square must have at least two diagonals, 
        // we can eliminate any Row/Col grouping that has less than 2 possible diagonal entries 
        listOfRowColDiagGroupings = possibleMagicSquares(listOfRowColDiagGroupings); 

        // We need two diagonals per row and column combination, and some row and column combinations have more than two possible diagonal entries.
        // Thus we perform a combination search to determine all two-pair diagonals that can coexist within the context of the row and column combination. 
        listOfRowColDiagGroupings = cleaningDiagonals(listOfRowColDiagGroupings); 

        // With this step complete we have generated a set of row and column and diagonal combinations (order does not matter).
        // Since we have not focused on order until this point, this set of row and column and diagonal combinations can be a representative 
        // set for the entire collection of magic squares of any size. 

        // That is, up to this point we have theoretically searched every possible solution (cleverly avoiding permutations that we know will never produce magic squares).
        // In other words this wraps up part 1 of the algorithm.
        // All that remains is to produce all the valid permutations of the elements in these row and column and diagonal combinations.
        
        /*
            END OF PART 1.
        */

        // The next step is by far the most complicated step because a lot happens in the next method. 
        // Unfortunately, there are some Row/Col/Diag combinations whose 2 diagonals cannot produce magic squares.
        // Determining which of these combinations are impossible magic squares also tells us how to construct the magic squares that are possible
        // Thus within this method there is another method that constructs a list of the initial magic squares.
        // This list of initial magic squares will then later be used to find all the other possible magic squares (ie: the inital magic squares are the representative squares for the entire set of magic squares of size n).
        List<int[][]> initalMagicSquares = new ArrayList<>();
        initalMagicSquares = filterImpossibleSquares(listOfRowColDiagGroupings, n); 

        // Using group theory and matrix multiplication, we produce all possible magic squares with the initalMagicSquares list. 
        List<int[][]> allMagicSquares = new ArrayList<>();
        allMagicSquares = completedMagicSquaresList(initalMagicSquares, n);

        System.out.println(toStringMagicSquares(allMagicSquares, n));
        System.out.println("Total Number of " + n + " by " + n + " Magic Squares: " + allMagicSquares.size());
        System.out.println("Are all outputs magic squares: " + checkOutput(allMagicSquares, magicValue));
        System.out.println("Are all the magic squares unique: " + checkForDuplicatesSquares(allMagicSquares));

        
        
    }

    // calculate how much the sum of each row/col/diag will equal to.
    public static int magicSum(int n){
        return (n * (n * n + 1))/2;
    }

    // create an arrayList of all the ints needed for magic squares.
    public static ArrayList<Integer> numbers(int n){
        ArrayList<Integer> nums = new ArrayList<>();
        for(int i = 1; i <= (n * n); i++){
            nums.add(i);
        }
        return nums;
    }

    // Now that all the unique row column combinations have been determined, brute force search for diagonals. 
    public static List<List<List<Set<Integer>>>> findDiagonals(List<List<List<Set<Integer>>>> rowCols, List<Set<Integer>> rows){
        List<List<List<Set<Integer>>>> completedSet = new ArrayList<>();
        List<List<Set<Integer>>> rowColDiag = new ArrayList<>();
        List<Set<Integer>> diagonalPiece = new ArrayList<>();
        int count = 0;

        // For each of the row column combinations.
        // We need to check that a given magicSum vector has a single shared element with each row vector and each column vector.
        // This condition matches what a diagonal is in a magic square (each element in the diagonal is shared once with each row vector and once with each column vector).

        for(List<List<Set<Integer>>> rowCol: rowCols){ // Loop through the row column combinations 
            diagonalPiece = new ArrayList<>(); // Reset the variable that keeps track of the magicSum vector
            for(Set<Integer> row: rows){ // Loop through all the magicSum vectors
                for(List<Set<Integer>> col: rowCol){ // For each individual row combination and each column combination within a row column combination (there will always only be one of each).
                    for(Set<Integer> set: col){  // For each vector in the row combination, or column combination (depending on where the previous loop is at).
                        count = 0; // Reset the count to keep track of similar elements.  
                        for(int n: row){ // For each integer in the magicSum vector.
                            if(set.contains(n)){ // If the current row or column vector from the row or column combination contains the element from the diagonal, increment the count. 
                                count++;
                            }
                        }
                        if(count != 1){ // If a single row or column vector was found to have less than or more than one element similarity, move onto the next magicSum vector. 
                            break;
                        }
                    }
                    if(count != 1){ // correctly moves to the next magicSum vector when the condition is failed.  
                        break;
                    } 
                }
                if(count == 1){ // If we get through a row column combination with the conditon being met through all the row and column vectors, add this magicSum vector as a possible diagonal for the row column combination. 
                    diagonalPiece.add(row); // Save the magicSum vector that can be a diagonal - this saves all the possible magicSum vectors into its own list.
                }   
            } 
            
                rowColDiag.add(rowCol.get(0)); // Add the row combination.
                rowColDiag.add(rowCol.get(1)); // Add the column combination.
                rowColDiag.add(diagonalPiece); // Add the MagicSum vector(s) that can be diagonals 
                completedSet.add(rowColDiag); // Add this entire set of row/col/diag candidates to a total list. 
                rowColDiag = new ArrayList<>(); // Reset the row/col/diag combinations for the next itteration. 
        }
        return completedSet; // Return a list of all the row and column combinations with their respective diagonal candidates.
    }

    // There are some row column combinations more or less than 2 diagonal candidates.
    // We only care aboue the row/column combinations that have two or more diagonals so return those
    public static List<List<List<Set<Integer>>>> possibleMagicSquares(List<List<List<Set<Integer>>>> groups){
        List<List<List<Set<Integer>>>> filtered = new ArrayList<>();
        for(int i = 0; i < groups.size(); i++){ // Loop through the previously determined list or row/col/diag candidates.
            if(groups.get(i).get(2).size() >= 2){ // If there are 2 or more diagonal candidates, 
                filtered.add(groups.get(i)); // Save these row/col/diag candidates and ignore the ones with less than two diagonals (since they can not [by definition - a magic square must have both diagonals sum to the magicSum] legally construct a magic square).
            }
        }

        return filtered; // Return a list of all the row/col/diag candidates with 2 or more diagonal candidates. 
    }

    // Since we only want two diagonals for each row column combination,
    // If there are more than two diagonals already,
    // Construct permuatations of the same row column combination with the permutations of all the two diagonal entries possible. 
    public static List<List<List<Set<Integer>>>> cleaningDiagonals(List<List<List<Set<Integer>>>> list){
        List<List<List<Set<Integer>>>> completedList = new ArrayList<>();
        List<List<Set<Integer>>> tempList = new ArrayList<>();
        List<Set<Integer>> diagonals = new ArrayList<>();
        for(List<List<Set<Integer>>> group: list){ // For each row/col/diag candidate
            for(int i = 0; i < group.get(2).size(); i++){ // For each diagonal within that row/col/diag candidate (outer loop) 
                for(int j = i + 1; j < group.get(2).size(); j++){ // For each other diagonal within that row/col/diag candidate (inner loop)
                    diagonals.add(group.get(2).get(i)); // Add the diagonal from the outer loop
                    diagonals.add(group.get(2).get(j)); // Add the diagonal from the inner loop
                    tempList.add(group.get(0)); // Add the row combination
                    tempList.add(group.get(1)); // Add the column combination
                    tempList.add(diagonals); // Add the permuted diagonal
                    completedList.add(tempList); // Add the new row/col/diag candidate to the list that keeps track of all of them. 
                    tempList = new ArrayList<>(); // Reset the row/col/diag candidate variable
                    diagonals = new ArrayList<>(); // Reset the diagonal vectors variable
                }
            }
        }
        return completedList; // Return a list of all the row/column/diag candidates (now each is unique and only has two diagonals).
    }

    // Now we want to check if it is possible to construct a magic square with the given row/col/diag candidates,
    // and if so, then we want to generate an inital magic square from the row/col/daig candidate. 
    public static List<int[][]> filterImpossibleSquares(List<List<List<Set<Integer>>>> cleanedList, int n){
        List<List<List<Set<Integer>>>> completedList = new ArrayList<>();
        HashMap<Integer, Integer> map = new HashMap<>();
        List<HashMap<Integer, Integer>> filteredMap = new ArrayList<>();
        
        boolean condition = true;
        for(List<List<Set<Integer>>> group: cleanedList){ // Loop through all of the row/col/diag candidates
            if(group.get(0).size() % 2 == 0){ // For the even case
                condition = true; // Reset the condition to check if possible
                for(int num: group.get(2).get(1)){ // Loop through the numbers in the first diagonal 
                    if(group.get(2).get(0).contains(num)){ // If any of the numbers in the first diagonal appear in the second diagonal then we have identified an impossible row/col/diag candidate (ie: the diagonals for even cases must be disjoint). 
                        condition = false; 
                        break; // Move onto the next row/col/diag candidate. 
                    }
                }
            } else{ // For the odd case
                condition = false;  // Reset the condition
                for(int num: group.get(2).get(1)){ // Loop through the numbers in the first diagonal. 
                    if(group.get(2).get(0).contains(num)){ // If a single number from the first diagonal is contained in the second diagonal (ie: the center has been identified).
                        condition = true; // We can now proceed for the odd case. 
                        break;
                    }
                }
            }

            if(condition){ // If the inital condition has been met for each respective size, then we may proceed. 

                // Now we want to check if a magic square can be constructed from the given row/col/daig candidate given the intial diagonal conditions have been met. 
                // In the paper that accompanies this code, I refer to this portion as the "loop" condition
                for(int firstDiagNum: group.get(2).get(0)){ // Loop through the elements in the first diagonal.
                    for(Set<Integer> row: group.get(0)){ // Loop through the row combination vecotrs.
                        if(row.contains(firstDiagNum)){ // If the row vector contains the diagonal element,
                            for(int colInnerNum: group.get(2).get(1)){ // Loop through the elements in the second diagonal.
                                if(row.contains(colInnerNum)){ // If this identifies row vector contains an element from the second element.
                                    map.put(firstDiagNum, colInnerNum); // We add a new entry into a hashmap from the element in the first diagonal to the element in the second diagonal.
                                    break; // We do this for every element in the first diagonal only. 
                                }
                            }
                        }
                    }
                }  
                // In the next few lines we do the same exact search as the previous search, except now we do it for the columns...
                for(int secondDiagNum: group.get(2).get(1)){ // Loop through the elements in the second diagonal. 
                    for(Set<Integer> col: group.get(1)){ // Loop through the column combination vectors.
                        if(col.contains(secondDiagNum)){ // If the column vector contains the diagonal element, 
                            for(int rowInnerNum: group.get(2).get(0)){ // Loop through the elements in the first diagnal.
                                if(col.contains(rowInnerNum)){ // If the column vector contains an element from the first diagonal,
                                    map.put(secondDiagNum, rowInnerNum); // We add a new entry into the hashmap from the element in the second diagonal to the elemnt in the first diagonal. 
                                    break; // We do this for every element in the second diagonal only. 
                                }
                            }
                        }
                    }
                }  
                // Now that we have identified the mapping from one diagonal to the other, we can check if the "loop condition is met".
                
                for(int testRow: group.get(2).get(0)){ // Loop through the elements in the first diagonal only.
                    int one = map.get(testRow); 
                    int two = map.get(one);
                    int three = map.get(two);
                    int four = map.get(three);
                    if(four != testRow){ // If an element doesn't loop back to itself after four hashmap iterations, then the "loop" condition is failed. 
                        condition = false;
                        break; // No need to keep searching through the diagonal since we have already identified an impossible row/col/diag candidate. 
                    }
                } 
                if(condition){ // If the "loop" condition is met
                    completedList.add(group); // Save the candidate into a list of all row/col/diag candidates that can/will make a magic square.
                    filteredMap.add(map); // Save the "loop" condition hashmap as this will tell us how to populate the magic square. 
                }
                map = new HashMap<>(); // Reset the hashmap for the next iteration 
            }
        }
        constructAMagicSquare(completedList, filteredMap, n);

        // Since this function is too long and I don't want to return both the groupings and the hashmaps that is the key to making magic squares,
        // I'm going to make another function that takes hashmaps and groupings and uses them to make magic squares. 

        return constructAMagicSquare(completedList, filteredMap, n); // Return the results from the next method (ie: make the inital set of magic squares with the previous info).
    }

    public static List<int[][]> constructAMagicSquare(List<List<List<Set<Integer>>>> combinations, List<HashMap<Integer, Integer>> filteredMap, int n){
        ArrayList<Integer> searchedThrough = new ArrayList<>();
        // Make the matrix.
        int[][] magicSquare = new int[n][n]; // first [] is rows second [] is columns ( we only need n - 1 bc we include 0).
        // Make the list that will carry all the matricies.
        List<int[][]> listOfMagicSquares = new ArrayList<>();

        // start populating the main daigonals (SEPERATE THESE LOOPS VIA ODD/EVEN).
        // I won't go into detail here but as a whole,
        // In this for loop we are going to populate the diagonals via the hashmap from the previous method. 
        for(int l = 0; l < combinations.size(); l++){
            int i = 0;
            for(int diagonal: combinations.get(l).get(2).get(0)){
                if(n % 2 == 0){ // even case
                    // iteration to insert via main daigonal
                    if(!searchedThrough.contains(diagonal)){
                        magicSquare[i][i] = diagonal;
                        magicSquare[i][(n - (i + 1))] = filteredMap.get(l).get(diagonal);
                        magicSquare[(n - (i + 1))][(n - (i + 1))] = filteredMap.get(l).get(magicSquare[i][(n - (i + 1))]);
                        magicSquare[(n - (i + 1))][i] = filteredMap.get(l).get(magicSquare[(n - (i + 1))][(n - (i + 1))]);
                        searchedThrough.add(magicSquare[i][i]);
                        searchedThrough.add(magicSquare[i][(n - (i + 1))]);
                        searchedThrough.add(magicSquare[(n - (i + 1))][(n - (i + 1))]);
                        searchedThrough.add(magicSquare[(n - (i + 1))][i]);
                        i++;
                    }
                    
                } else { // odd case
                    // its the same as the even case except we need to take into account the center
                    if(filteredMap.get(l).get(diagonal) == diagonal){ // this is the center
                        magicSquare[n/2][n/2] = diagonal;
                        searchedThrough.add(diagonal);
                    } else {
                        if(!searchedThrough.contains(diagonal)){
                            magicSquare[i][i] = diagonal;
                            magicSquare[i][(n - (i + 1))] = filteredMap.get(l).get(diagonal);
                            magicSquare[(n - (i + 1))][(n - (i + 1))] = filteredMap.get(l).get(magicSquare[i][(n - (i + 1))]);
                            magicSquare[(n - (i + 1))][i] = filteredMap.get(l).get(magicSquare[(n - (i + 1))][(n - (i + 1))]);
                            searchedThrough.add(magicSquare[i][i]);
                            searchedThrough.add(magicSquare[i][(n - (i + 1))]);
                            searchedThrough.add(magicSquare[(n - (i + 1))][(n - (i + 1))]);
                            searchedThrough.add(magicSquare[(n - (i + 1))][i]);
                            i++;
                        }
                    }

                }
            } 
            listOfMagicSquares.add(magicSquare);
            searchedThrough = new ArrayList<>();
            magicSquare = new int[n][n];
        }

        // Up to this point all the diagonals have been set. 
        // All the off diagonals have a value of 0 (which is less than any of the values a magic square can have by definition).
        for(int j = 0; j < listOfMagicSquares.size(); j++){ // Loop through the list of all possible magic squares.
            for(int k = 0; k < n; k++){ // Loop through the rows
                for(int m = 0; m < n; m++){ // Loop through the colums
                    if(listOfMagicSquares.get(j)[k][m] == 0){ // If the particular positon at this particular magic square is zero,
                        // We need to identfy the coresponding row and column vector that fit into this particular position...
                        // Then we need to identify the common element within this specific row and column vector and place that into the particular position.
                        for(Set<Integer> rows: combinations.get(j).get(0)){ // Loop through the row vectors.
                            for(Set<Integer> columns: combinations.get(j).get(1)){ // Loop through the column vectors. 
                                if(rows.contains(listOfMagicSquares.get(j)[k][k]) && columns.contains(listOfMagicSquares.get(j)[m][m])){ // If the row vector contains the corresponding diagonal element and the column vector contains the corresponding diagonal element, we have identified the row/column vectors for the particular positon.
                                    for(int num: rows){  // Now loop through the elements in that row vector
                                        if(columns.contains(num)){ // If the column vector contains the element from the row vector, we have identified what integer value takes place over the zero. 
                                            listOfMagicSquares.get(j)[k][m] = num; // Replace the zero with that value.
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // At this point we have effectively eliminated all the zeros and constructed a base set of magic squares. 
        return listOfMagicSquares; // Return the base set of completed and unique magic squares. 
    }

    // With a completed set of unique magic squares, we need to transform them to get all of the possible magic squares.

    // To do this we being with a simple method that takes two matrices as input and returns their product.
    public static int[][] matrixMultiply(int[][] matrixOne, int[][] matrixTwo, int n){ // always multiply matrixone into matrix two
        int[][] matrixThree = new int[n][n];

        for(int i = 0; i < n; i++){ 
            for(int j = 0; j < n; j++){ 
                for(int k = 0; k < n; k++){ 
                    matrixThree[i][j] += matrixOne[i][k] * matrixTwo[k][j];
                }
            }
        }
        return matrixThree;
    }

    // For purposes of transforming magic squares, we also need a method that can reflect them. 
    public static int[][] reflection(int n){
        int[][] reflection = new int[n][n];

        for(int i = 0; i < n; i ++){
            reflection[i][n - (i + 1)] = 1;
        }

        return reflection;
    }

    // Finally we need another method that will produce the transpose of a matrix. 
    public static int[][] transpose(int[][] initalMatrix, int n){
        int[][] transpose = new int[n][n];

        for(int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                transpose[i][j] = initalMatrix [j][i];
            }
        }
        return transpose;
    }

    // Given a base set of unique magic squares, compute all the opportations necessary from the 3 previous methods.  
    public static List<int[][]> completedMagicSquaresList(List<int[][]> initialList, int n){
        List<int[][]> firstOutput = new ArrayList<>();
        List<int[][]> finalOutput = new ArrayList<>();

        int[][] reflection = reflection(n);
        int[][] kleinOne = {{0,0,1,0}, {1,0,0,0}, {0,0,0,1}, {0,1,0,0}};
        int[][] kleinTwo = {{1,0,0,0}, {0,0,1,0}, {0,1,0,0}, {0,0,0,1}};
        int[][] kleinThree = {{0,1,0,0}, {1,0,0,0}, {0,0,0,1}, {0,0,1,0}};
        if(n == 4){
            for(int i = 0; i < initialList.size(); i++){
                firstOutput.add(initialList.get(i));
                firstOutput.add(matrixMultiply(matrixMultiply(kleinOne, initialList.get(i), n), kleinOne, n));
                firstOutput.add(matrixMultiply(matrixMultiply(kleinTwo, initialList.get(i), n), kleinTwo, n));
                firstOutput.add(matrixMultiply(matrixMultiply(kleinThree, initialList.get(i), n), kleinThree, n));
            }
        } else { // for now we do nothing for other sizes. 
            for(int i = 0; i < initialList.size(); i++){
                firstOutput.add(initialList.get(i));
            }
        }

        for(int i = 0; i < firstOutput.size(); i++){
            int[][] r1 = firstOutput.get(i); // identity
            int[][] r2 = matrixMultiply(transpose(r1, n), reflection, n); // rotation by 90 degrees
            int[][] r3 = matrixMultiply(transpose(r2, n), reflection, n); // rotation by 180 degrees
            int[][] r4 = matrixMultiply(transpose(r3,n), reflection, n); // rotation by 270 degrees
            int[][] sr1 = matrixMultiply(r1, reflection, n); // vertical reflection
            int[][] sr2 = matrixMultiply(transpose(sr1, n), reflection, n); // vertical reflection + 90 degrees
            int[][] sr3 = matrixMultiply(transpose(sr2, n), reflection, n); // vertical reflection + 180 degrees
            int[][] sr4 = matrixMultiply(transpose(sr3, n), reflection, n); // vertical reflection + 270 degrees
            finalOutput.add(r1);
            finalOutput.add(r2);
            finalOutput.add(r3);
            finalOutput.add(r4);
            finalOutput.add(sr1);
            finalOutput.add(sr2);
            finalOutput.add(sr3);
            finalOutput.add(sr4);
        }
        return finalOutput; // Return a list of all possible magic squares. 
    }

    public static boolean checkOutput(List<int[][]> listOfMagicSquares, int magicValue){ // This method simply checkes that all the magic squares are in fact magic squares by calculating the sum of the rows, columns, and diagonals and checking if they all equal the magic sum.
        boolean condition = true;
        int sumRows = 0;
        int sumCols = 0;
        int sumMainDiag = 0;
        int sumOffDiag = 0;

        for(int[][] square: listOfMagicSquares){
            for(int i = 0; i < square.length; i++){
                sumMainDiag += square[i][i];
                sumOffDiag += square[i][square.length - 1 - i];
                for(int j = 0; j < square.length; j++){
                    sumRows += square[i][j];
                    sumCols += square[j][i];
                }
                if(sumRows != magicValue || sumCols != magicValue){
                    condition = false;
                    break;
                }
                sumRows = 0;
                sumCols = 0;
                
            }
            if(sumMainDiag != magicValue || sumOffDiag != magicValue || !condition){
                condition = false;
                break;
            }
            sumMainDiag = 0;
            sumOffDiag = 0;
        }
        return condition;
    }

    public static boolean checkForDuplicatesSquares(List<int[][]> listOfMagicSquares){ // This method makes sure that no duplicates were constructed after the transformations on the base set of magic squares. 
        boolean condition = true; 
        for(int i = 0; i < listOfMagicSquares.size(); i++){
            for(int j = i + 1; j < listOfMagicSquares.size(); j++){
                condition = true; // reset the condition for each new square checked. 
                for(int k = 0; k < listOfMagicSquares.get(i).length; k++){
                    for(int l = 0; l < listOfMagicSquares.get(i).length; l++){
                        if(listOfMagicSquares.get(i)[k][l] != listOfMagicSquares.get(j)[k][l]){
                            condition = false; // move onto the next square
                            break; 
                        }
                    }
                    if(!condition){
                        break;
                    }
                }
                if(condition){ // if we never broke (ie a duplicat was found)
                    System.out.println(Arrays.deepToString(listOfMagicSquares.get(i)) + "\n" + Arrays.deepToString(listOfMagicSquares.get(j)));
                    break;
                }   
            }
            if(condition){
                break;
            }
        }
        return !condition;
    }
    
    public static String toString(List<Set<Integer>> list){ // This method lets you convert a List<Set<integer>> into a string that's easier to read.
        String str = "";
        for(Set<Integer> set: list){
            str += set.toString() + "\n"; 
        }
        return str;
    }

    public static String toStringAll(List<List<Set<Integer>>> list){ // This method lets you convert a List<List<Set<integer>>> into a string that's easier to read.
        String str = "";
        for(List<Set<Integer>> group: list){
            str += group.toString() + "\n";
        }
        return str;
    }

    public static String toStringRowsCols(List<List<List<Set<Integer>>>> all){ // This method lets you convert a List<List<Set<integer>>>> into a string that's easier to read.
        String str = "";
        for(int i = 0; i < all.size(); i++){
            for(int j = 0; j < all.get(i).size(); j++){
                if(j == 0){
                    str += "Rows: " + all.get(i).get(j);
                } else if(j == 1){
                    str +=" Columns: " + all.get(i).get(j) + "\n";
                } else{
                    str +="Diagonals: " + all.get(i).get(j) + "\n\n";
                }
            }
        }
        return str;
    }

    public static String toStringMagicSquares(List<int[][]> squares, int n){ // This method lets us output final the results cleanly. 
        String str = "";
        for(int i = 0; i < squares.size(); i++){
            for(int j = 0; j < n; j++){
                str += Arrays.toString(squares.get(i)[j])+"\n";
            }
            str += "\n";
        }
        return str;
    }

    
}