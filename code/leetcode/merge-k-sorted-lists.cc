/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode(int x) : val(x), next(NULL) {}
 * };
 */

#include <queue>
using namespace std;

struct ListNode {
  int val;
  ListNode* next;
  ListNode(int x) : val(x), next(NULL) {}
};

class Solution {
public:
  class Elem {
  public:
    int v;
    ListNode* p;
    bool operator <(const Elem& x) const {
      return x.v < v;
    }
    Elem(int v, ListNode* p): v(v), p(p) {}
  };
  ListNode* mergeKLists(vector<ListNode*>& lists) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    priority_queue<Elem> Q;
    for(int i = 0; i < lists.size(); i++) {
      ListNode* x = lists[i];
      if(x) {
        Q.push(Elem(x->val, x));
      }
    }
    ListNode* prev = NULL;
    ListNode* head = NULL;
    while(!Q.empty()) {
      Elem e = Q.top();
      ListNode* n = e.p;
      Q.pop();
      if(prev == NULL) {
        prev = n;
        head = n;
      } else {
        prev->next = n;
        prev = n;
      }
      n = n->next;
      if(n) {
        Q.push(Elem(n->val, n));
      }
    }
    if(prev) {
      prev->next = NULL;
    }
    return head;
  }
};

int main() {
  return 0;
}
