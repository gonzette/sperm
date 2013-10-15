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
  ListNode* swapPairs(ListNode* head) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    ListNode* prev = NULL;
    ListNode* res = NULL;
    while(head) {
      if(head && head->next) {
        ListNode* p = head;
        ListNode* q = head->next;
        head = head->next->next;
        q->next = p;
        if(prev == NULL) {
          res = q;
        } else {
          prev -> next = q;
        }
        prev = p;
      } else if(head) {
        if(prev == NULL) {
          res = head;
        } else {
          prev -> next = head;
        }
        prev = head;
        break; // last one.
      }
    }
    if(prev) {
      prev -> next = NULL;
    }
    return res;
  }
};

int main() {
  return 0;
}
