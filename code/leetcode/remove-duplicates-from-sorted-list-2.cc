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
  ListNode* deleteDuplicates(ListNode* head) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(head == NULL) return NULL;
    ListNode* xhead = NULL;
    ListNode* xprev = NULL;
    bool dup = false;
    for(;;) {
      ListNode* next = head->next;
      if(next == NULL) break;
      if(head->val == next->val) {
        dup = true;
      } else {
        if(!dup) {
          if(!xhead) xhead = head;
          if(xprev) {
            xprev->next = head;
          }
          xprev = head;
          xprev->next = NULL;
        }
        dup = false;
      }
      head = next;
    }
    if(!dup) {
      if(!xhead) xhead = head;
      if(xprev) {
        xprev->next = head;
      }
      xprev = head;
      xprev->next = NULL;
    }
    return xhead;
  }
};

int main() {
  return 0;
}
