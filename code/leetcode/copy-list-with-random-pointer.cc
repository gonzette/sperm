/**
 * Definition for singly-linked list with a random pointer.
 * struct RandomListNode {
 *     int label;
 *     RandomListNode *next, *random;
 *     RandomListNode(int x) : label(x), next(NULL), random(NULL) {}
 * };
 */

#include <map>
using namespace std;
struct RandomListNode {
  int label;
  RandomListNode* next, *random;
  RandomListNode(int x) : label(x), next(NULL), random(NULL) {}
};

class Solution {
public:
  map<RandomListNode*, RandomListNode*> cache;
  RandomListNode* copyRandomList(RandomListNode* head) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(head == NULL) {
      return NULL;
    }
    RandomListNode* x = cache[head];
    if(x != NULL) {
      return x;
    }
    x = new RandomListNode(head->label);
    cache[head] = x;
    x->next = copyRandomList(head->next);
    x->random = copyRandomList(head->random);
    return x;
  }
};


int main() {
  return 0;
}
