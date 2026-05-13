package org.bad_coders.better_bundles;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.class_1792;
import net.minecraft.class_1799;

public class BundleCapacityHolder {
   private static final ThreadLocal<Deque<Integer>> CAPACITY_STACK = ThreadLocal.withInitial(ArrayDeque::new);

   public static void push(int capacity) {
      ((Deque)CAPACITY_STACK.get()).push(capacity);
   }

   public static void setup(class_1799 stack) {
      setup(stack.method_7909());
   }

   public static void setup(class_1792 item) {
      push(64 + BundleUpgradeTier.getTierFromItem(item).totalExtraSlots);
   }

   public static int get() {
      Integer val = (Integer)((Deque)CAPACITY_STACK.get()).peek();
      return val != null ? val : 64;
   }

   public static boolean hasContext() {
      return ((Deque)CAPACITY_STACK.get()).peek() != null;
   }

   public static void pop() {
      Deque<Integer> stack = (Deque)CAPACITY_STACK.get();
      if (!stack.isEmpty()) {
         stack.pop();
      }

   }

   public static void set(int capacity) {
      push(capacity);
   }

   public static void reset() {
      pop();
   }
}
