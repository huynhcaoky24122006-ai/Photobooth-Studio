package com.example.photoboothstudio.models;

import java.io.Serializable;

/**
 * RewardClaim đại diện cho đối tượng Lịch sử điểm danh nhận Xu
 * Lưu vết mỗi lần người dùng điểm danh hàng ngày thành công
 */
public class RewardClaim implements Serializable {
    private String id;           // ID duy nhất của lượt điểm danh (vd: "claim_user123_20261003")
    private String userId;       // ID người dùng thực hiện điểm danh
    private int rewardCoins;     // Số Xu nhận được (vd: 10, 20, 50)
    private String claimDate;    // Ngày điểm danh định dạng "YYYY-MM-DD" để check trùng trùng lặp trong ngày
    private long claimedAt;      // Timestamp thời điểm điểm danh (milliseconds)

    // Đừng xóa constructor rỗng
    public RewardClaim() {
    }

    // Constructor đầy đủ tham số
    public RewardClaim(String id, String userId, int rewardCoins, String claimDate, long claimedAt) {
        this.id = id;
        this.userId = userId;
        this.rewardCoins = rewardCoins;
        this.claimDate = claimDate;
        this.claimedAt = claimedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(int rewardCoins) {
        this.rewardCoins = rewardCoins;
    }

    public String getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(String claimDate) {
        this.claimDate = claimDate;
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(long claimedAt) {
        this.claimedAt = claimedAt;
    }
}