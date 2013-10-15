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
    if(root == NULL) {
      return ;
    }
    TreeLinkNode* saved = NULL;
    TreeLinkNode* head = NULL;
    while(root) {
      //printf("%p\n",root);
      TreeLinkNode* l = root->left;
      TreeLinkNode* r = root->right;
      if(l == NULL && r == NULL) {
        // pass.
      } else if(l == NULL) {
        if(head != NULL) {
          head->next = r;
        } else {
          saved = r;
        }
        r->next = NULL;
        head = r;
      } else if(r == NULL) {
        if(head != NULL) {
          head->next = l;
        } else {
          saved = l;
        }
        l->next = NULL;
        head = l;
      } else {
        if(head != NULL) {
          head->next = l;
        } else {
          saved = l;
        }
        //printf("l = %p, r = %p, saved=%p\n",l,r,saved);
        l->next = r;
        r->next = NULL;
        head = r;
      }
      root = root->next;
    }
    R(saved);
  }
};

int main() {
  TreeLinkNode* x1 = new TreeLinkNode(1);
  TreeLinkNode* x2 = new TreeLinkNode(2);
  TreeLinkNode* x3 = new TreeLinkNode(3);
  TreeLinkNode* x4 = new TreeLinkNode(4);
  TreeLinkNode* x5 = new TreeLinkNode(5);

  x2->left = x4;
  x2->right = x5;
  x1->left = x2;
  x1->right = x3;

  Solution s;
  s.connect(x1);

  printf("x2=%p,x2->next=%p(%p),x3->next=%p\n", x2, x2->next, x3, x3->next);
  printf("x4=%p,x4->next=%p(%p)\n", x4, x4->next, x5);
  return 0;
}
