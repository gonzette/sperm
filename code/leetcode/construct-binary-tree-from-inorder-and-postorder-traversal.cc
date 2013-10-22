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
  TreeNode* buildTree(vector<int>& inorder, vector<int>& postorder) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    return R(inorder, 0, inorder.size() - 1, postorder, 0, postorder.size() - 1);
  }
  TreeNode* R(const vector<int>& inorder, int is, int ie,
              const vector<int>& postorder, int ps, int pe) {
    if(ps > pe) return NULL;
    if(ps == pe) return new TreeNode(postorder[ps]);
    int x = postorder[pe];
    int i = is;
    for(; i <= ie; i++) {
      if(inorder[i] == x) break;
    }
    TreeNode* root = new TreeNode(x);
    root->left = R(inorder, is, i - 1, postorder, ps, (i - is) + ps - 1);
    root->right = R(inorder, i + 1, ie,  postorder, (i - is) + ps, pe - 1);
    return root;
  }
};

int main() {
  return 0;
}
