import java.util.*;

public class Main {
    public static void main(String[] args) {

        int[] tmp =new int[2];
        String e = "`12";
//        Scanner in = new Scanner(System.in);
//        ArrayList<Integer> ar = new ArrayList<>();
//        for(;in.hasNext();){
//            ar.add(in.nextInt());
//        }
        int[] nums = new int[]{5,1,3};
        Solution test= new Solution();
        test.longestValidParentheses("()(())");
        ArrayList
     }
    static class Solution {
        public int longestValidParentheses(String s) {
            int size = s.length();
            int[] dp = new int[size];
            int res = 0;
            for(int i = 1; i < size; ++i){
                if(s.charAt(i) == ')' && i-dp[i-1]-1 >= 0 && s.charAt(i-dp[i - 1 ]-1) == '('){
                    if(i-dp[i-1]-2 >= 0)
                        dp[i] = dp[i-1] + dp[i-dp[i-1]-2] + 2;
                    else
                        dp[i] = dp[i-1] + 2;
                    res = dp[i] > res?dp[i] : res;
                }
            }
            return res;
        }
    }
}