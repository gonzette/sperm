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
#include <queue>
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
  vector<vector<int> > levelOrder(TreeNode* root) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    queue<TreeNode*> S[2];
    int sw = 0;
    vector< vector<int> > res;
    if(root == NULL) return res;
    S[sw].push(root);
    while(!S[sw].empty()) {
      vector<int> vs;
      while(!S[sw].empty()) {
        TreeNode* p = S[sw].front();
        S[sw].pop();
        vs.push_back(p->val);
        if(p->left) S[1 - sw].push(p->left);
        if(p->right) S[1 - sw].push(p->right);
      }
      res.push_back(vs);
      sw = 1 - sw;
    }
    return res;
  }
};

int main() {
  return 0;
}
