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
    ListNode* p = head;
    ListNode* prev = head;
    ListNode* q = p->next;
    while(q) {
      if(q->val == prev->val) {
        // pass;
      } else {
        prev->next = q;
        prev = q;
      }
      q = q->next;
    }
    prev->next = NULL;
    return p;
  }
};

int main() {
  return 0;
}
