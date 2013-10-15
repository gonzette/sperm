/**
 * Definition for singly-linked list.
 * struct ListNode {
 *     int val;
 *     ListNode *next;
 *     ListNode(int x) : val(x), next(NULL) {}
 * };
 */
/**
 * Definition for binary tree
 * struct TreeNode {
 *     int val;
 *     TreeNode *left;
 *     TreeNode *right;
 *     TreeNode(int x) : val(x), left(NULL), right(NULL) {}
 * };
 */

#include <cstdio>
struct ListNode {
  int val;
  ListNode* next;
  ListNode(int x) : val(x), next(NULL) {}
};

struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};

class Solution {
public:
  TreeNode* sortedListToBST(ListNode* head) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    if(head == NULL) {
      return NULL;
    }
    R(head);
  }

  TreeNode* R(ListNode* head) {
    if(head -> next == NULL) {
      return new TreeNode(head->val);
    } else if(head -> next -> next == NULL) {
      TreeNode* s = new TreeNode(head->val);
      TreeNode* r = new TreeNode(head->next->val);
      s->right = r;
      return s;
      // r->left = s;
    } else {
      ListNode* prev = NULL;
      ListNode* s1 = head;
      ListNode* s2 = head;
      for(;;) {
        prev = s1;
        s1 = s1->next;
        if(!s2->next || !s2->next->next) {
          break;
        }
        s2 = s2->next->next;
        if(!s2->next) {
          break;
        }
      }
      // split from s1.
      prev->next = NULL;
      TreeNode* s = new TreeNode(s1->val);
      s->left = R(head);
      s->right = R(s1->next);
      return s;
    }
  }
};

int main() {
  return 0;
}
