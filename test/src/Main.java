import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        int[] tmp =new int[2];
        String e = "`12";
        Scanner in = new Scanner(System.in);
        LRUCache test = new LRUCache(2);
        test.put(1,1);
        test.put(2,2);
        test.get(1);
        test.put(3,3);
        test.get(2);
        test.put(4,4);
        test.get(1);
        test.get(3);
        test.get(4);

     }
    static class LRUCache {
        class Node{
            int num;
            Node next,pre;
            Node(int num,Node pre,Node next){
                this.num = num;
                this.pre = pre;
                this.next = next;
            }
        }
        Map<Integer,Node> mp;
        int maxNum =0 ,nowNum = 0;
        Node headNode;
        public LRUCache(int capacity) {
            maxNum = capacity;
            mp = new HashMap<>();
            headNode = new Node(0,null,null);
        }

        public int get(int key) {
            Node res = mp.get(key);
            if(res != null){
                res.pre.next = res.next;
                res.next.pre = res.pre;

                res.next = headNode.next;
                res.pre = headNode;
                headNode.next.pre = res;
                headNode.next = res;

                return res.num;
            }
            else{
                return -1;
            }
        }

        public void put(int key, int value) {
            if(get(key) == -1){
                if(nowNum == maxNum){
                    Node needRemove = headNode.pre;
                    needRemove.pre.next=needRemove.next;
                    needRemove.next.pre=needRemove.pre;
                    nowNum--;
                }
                Node node = new Node(value,headNode,headNode.next);
                mp.put(key,node);
                if(nowNum == 0){
                    headNode.next = node;
                    headNode.pre = node;
                    node.pre = headNode;
                    node.next = headNode;
                }
                else{
                    headNode.next.pre = node;
                    headNode.next = node;
                }
                nowNum++;
            }
            else{
                mp.get(key).num = value;
            }
        }
    }
}