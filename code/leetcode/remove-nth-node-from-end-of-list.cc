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
  ListNode* removeNthFromEnd(ListNode* head, int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    ListNode* prev = NULL;
    ListNode* x = head;
    ListNode* y = head;
    for(int i = 0; i < n - 1; i++) {
      y = y->next;
    }
    while(y->next) {
      prev = x;
      x = x->next;
      y = y->next;
    }
    if(prev == NULL) {
      return head->next;
    } else {
      prev -> next = x->next;
      return head;
    }
  }
};

int main() {
  return 0;
}
