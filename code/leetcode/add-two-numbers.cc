/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode(int x) : val(x), next(NULL) {}
 * };
 */

#include <cstdio>
struct ListNode {
  int val;
  ListNode* next;
  ListNode(int x) : val(x), next(NULL) {}
};


class Solution {
public:
  ListNode* addTwoNumbers(ListNode* l1, ListNode* l2) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(!l1 && !l2) return NULL;
    if(!l1) return l2;
    if(!l2) return l1;

    ListNode* prev = NULL;
    ListNode* head = NULL;
    int b = 0;
    while(l1 && l2) {
      int v = l1->val + l2->val + b;
      if(v >= 10) {
        b = 1;
        v -= 10;
      } else {
        b = 0;
      }
      ListNode* t = new ListNode(v);
      if(prev == NULL) {
        head = t;
      } else {
        prev -> next = t;
      }
      prev = t;
      l1 = l1->next;
      l2 = l2->next;
    }
    ListNode* n = l1;
    if(!l1) n = l2;
    while(n) {
      int v = n->val + b;
      if(v >= 10) {
        b = 1;
        v -= 10;
      } else {
        b = 0;
      }
      ListNode* t = new ListNode(v);
      prev->next = t;
      prev = t;
      n = n->next;
    }
    if(b) {
      ListNode* t = new ListNode(b);
      prev->next = t;
      prev = t;
    }

    return head;
  }
};

int main() {
  return 0;
}
