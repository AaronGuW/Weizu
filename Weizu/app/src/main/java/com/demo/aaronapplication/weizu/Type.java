package com.demo.aaronapplication.weizu;

/**
 * Created by Aaron on 2016/3/21.
 */
public class Type {

    public static enum books {
        math,
        computer,
        construction,
        architecture,
        english,
        politics;

        @Override
        public String toString() {
            switch (this.name()) {
                case ("math"):
                    return "数学类";
                case ("computer"):
                    return "计算机类";
                case ("construction"):
                    return "土木类";
                case ("architecture"):
                    return "建筑类";
                case ("english"):
                    return "英语类";
                case ("politics"):
                    return "政治类";
                default:
                    return null;
            }
        }
    }

    public static enum electronics {
        cellphone,
        laptop,
        camera,
        tablet,
        ebook,
        headphone;

        @Override
        public String toString() {
            switch (this.name()) {
                case ("cellphone"):
                    return "手机";
                case ("laptop"):
                    return "笔记本";
                case ("camera"):
                    return "单反";
                case ("tablet"):
                    return "平板电脑";
                case ("ebook"):
                    return "电子书";
                case ("headphone"):
                    return "耳机";
                default:
                    return null;
            }
        }
    }

    public static enum transportation {
        bike,
        unicycle,
        motionbike,
        auto,
        skateboard,
        roller;

        @Override
        public String toString() {
            switch (this.name()) {
                case "bike":
                    return "自行车";
                case "unicycle":
                    return "电动独轮车";
                case "motionbike":
                    return "体感车";
                case "auto":
                    return "汽车";
                case "skateboard":
                    return "滑板";
                case "roller":
                    return "轮滑鞋";
                default:
                    return null;
            }
        }
    }

    public static enum clothes {
        costume,
        schooluniform,
        suit,
        eveningdressing,
        cosplay,
        dailyoutfit;

        @Override
        public String toString() {
            switch (this.name()) {
                case ("costume"):
                    return "表演服装";
                case ("schooluniform"):
                    return "校服";
                case ("suit"):
                    return "正装";
                case ("eveningdressing"):
                    return "晚装";
                case ("cosplay"):
                    return "cosplay服饰";
                case ("dailyoutfit"):
                    return "日常服装";
                default:
                    return null;
            }
        }
    }

    public static enum sports {
        ball,
        team,
        fitness,
        outdoor,
        accessory,
        wearable;

        @Override
        public String toString() {
            switch (this.name()) {
                case "ball":
                    return "球类";
                case "team":
                    return "组队装备";
                case "fitness":
                    return "健身器材";
                case "outdoor":
                    return "户外装备";
                case "accessory":
                    return "运动配件";
                case "wearable":
                    return "可穿戴设备";
                default:
                    return null;
            }
        }
    }

    public static enum chemical {
        makeup,
        makeuptools,
        hairdressing,
        nail,
        bodycare,
        scream;

        @Override
        public String toString() {
            switch (this.name()) {
                case "makeup":
                    return "彩妆";
                case "tools":
                    return "化妆工具";
                case "hairdressing":
                    return "美发工具";
                case "nail":
                    return "美甲工具";
                case "bodycare":
                    return "美体工具";
                case "scream":
                    return "护肤品";
                default:
                    return null;
            }
        }
    }

    public static enum activity {
        tent,
        portablefurniture,
        audio,
        boardgame,
        BBQ,
        daily;

        @Override
        public String toString() {
            switch (this.name()) {
                case "tent":
                    return "帐篷";
                case "portablefurniture":
                    return "便携桌椅";
                case "audio":
                    return "音箱";
                case "boardgame":
                    return "桌游";
                case "bbq":
                    return "烧烤工具";
                case "daily":
                    return "日用品";
                default:
                    return null;
            }
        }
    }

    public static enum others {
        audiovisual,
        kitchen,
        software,
        musicinstrument,
        appliance,
        office;

        @Override
        public String toString() {
            switch (this.name()) {
                case "audiovisual":
                    return "音像制品";
                case "kitchen":
                    return "厨具";
                case "software":
                    return "软件";
                case "musicinstrument":
                    return "乐器";
                case "appliance":
                    return "家电";
                case "office":
                    return "办公用品";
                default:
                    return null;
            }
        }
    }
}
