#include <cstdio>

/**
 * Definition for binary tree with next pointer.
 * struct TreeLinkNode {
 *  int val;
 *  TreeLinkNode *left, *right, *next;
 *  TreeLinkNode(int x) : val(x), left(NULL), right(NULL), next(NULL) {}
 * };
 */

struct TreeLinkNode {
  int val;
  TreeLinkNode* left, *right, *next;
  TreeLinkNode(int x) : val(x), left(NULL), right(NULL), next(NULL) {}
};

class Solution {
public:
  void connect(TreeLinkNode* root) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    if(root == NULL) {
      return ;
    }
    root->next = NULL;
    R(root);
  }

  void R(TreeLinkNode* root) {
    // leaf.
    if(root->left == NULL) {
      return ;
    }
    TreeLinkNode* saved = root;
    TreeLinkNode* head = NULL;
    while(root) {
      TreeLinkNode* l = root->left;
      TreeLinkNode* r = root->right;
      if(head != NULL) {
        head->next = l;
      }
      l->next = r;
      r->next = NULL;
      head = r;
      root = root->next;
    }
    R(saved->left);
  }
};

int main() {
  return 0;
}
