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
  ListNode* reverseBetween(ListNode* head, int m, int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    ListNode* xhead = NULL;
    ListNode* xprev = NULL;
    ListNode* ytail = NULL;
    ListNode* yprev = NULL;

    int cnt = 0;
    while(head) {
      cnt++;
      ListNode* next = head->next;
      if(cnt >= m && cnt <= n) {
        if(ytail == NULL) ytail = head;
        head->next = yprev;
        yprev = head;
      } else if(cnt < m) {
        if(xhead == NULL) xhead = head;
        xprev = head;
      } else {
        break;
      }
      head = next;
    }
    ListNode* res = NULL;
    if(m == 1) {
      res = yprev;
    } else {
      res = xhead;
      xprev->next = yprev;
    }
    ytail->next = head;
    return res;
  }
};


int main() {
  return 0;
}
