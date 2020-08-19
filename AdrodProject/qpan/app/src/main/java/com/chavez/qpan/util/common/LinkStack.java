package com.chavez.qpan.util.common;

/**
 * @Author Chavez Qiu
 * @Date 19-12-27.
 * Email：qiuhao1@meizu.com
 * Description：单链表栈  Singly linked lists of stack
 */
public class LinkStack<T> {

    private Node top;
    private int size;

    public void push(T value) {
        Node node = new Node(value);
        if (top == null) {
            top = node;
        } else {
            node.next = top;
            top = node;
        }
        size++;
    }

    public T pull() {
        if (top != null) {
            T value = top.value;
            top = top.next;
            size--;
            return value;
        }
        return null;
    }

    public T getTopValue() {
        return top == null ? null : top.value;
    }

    public  int getStackSize(){
      return size;
    }

    private class Node {
        Node next;
        T value;

        public Node(T v) {
            value = v;
            next = null;
        }
    }
}
