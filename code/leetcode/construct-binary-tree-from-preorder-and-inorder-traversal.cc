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
#include <vector>
using namespace std;

struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};

class Solution {
public:
  TreeNode* buildTree(vector<int>& preorder, vector<int>& inorder) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    return R(preorder, 0, preorder.size() - 1, inorder, 0, inorder.size() - 1);
  }
  TreeNode* R(const vector<int>& preorder, int ps, int pe,
              const vector<int>& inorder, int is, int ie) {
    if(ps > pe) return NULL;
    if(ps == pe) return new TreeNode(preorder[ps]);
    int x = preorder[ps];
    int i = is;
    for(; i <= ie; i++) {
      if(inorder[i] == x) break;
    }
    TreeNode* root = new TreeNode(x);
    root->left = R(preorder, ps + 1, (i - is) + ps, inorder, is, i - 1);
    root->right = R(preorder, (i - is) + ps + 1, pe, inorder, i + 1, ie);
    return root;
  }
};

int main() {
  return 0;
}
