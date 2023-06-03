// 获取每列列表dom
const listBoxs = document.querySelectorAll('.imgWrap')
const winDom = document.querySelector('.winBox')
const startBtn = document.getElementById('goBtn')
// 抽奖数据
const prizeList = [
    { name: 'J', value: '1', img: './img/1.png' },
    { name: 'K', value: '2', img: './img/2.png' },
    { name: 'A', value: '3', img: './img/3.png' },
    { name: 'Q', value: '4', img: './img/4.png' },
    { name: '7', value: '5', img: './img/5.png' },
    { name: '铃', value: '6', img: './img/6.png' },
    { name: '西瓜', value: '7', img: './img/7.png' },
    { name: '茄子', value: '8', img: './img/8.png' },
    { name: '樱桃', value: '9', img: './img/9.png' },
    { name: '葡萄', value: '10', img: './img/10.png' },
    { name: '柠檬', value: '11', img: './img/11.png' },
    { name: '橘子', value: '12', img: './img/12.png' },
]
// 获取抽奖结果（采用随机数获取抽奖列表下标）
const getResults = () => {
    const probability = 0.3    //概率
    const max = prizeList.length - 1;
    let arr = [];
    let num = 3
    const flag = Math.ceil(Math.random() * 10) / 10 <= probability
    console.log(flag)
    if (flag) {
        const index = Math.floor(Math.random() * (max + 1))
        arr = [index, index, index]
    } else {
        for (let i = 0; i < num; i++) {
            const index = Math.floor(Math.random() * (max + 1))
            if (i == 2 && (arr[0] === arr[1] && arr[1] === index)) {
                i -= 1
            } else {
                arr.push(index)
            }
        }
    }
    return {
        flag,
        arr,
    }
}


// 游戏类 
class Slot {
    constructor(prizeList, duration = 4000, delay = 300) {
        this.prizeList = prizeList
        this.shaftList = []
        this.duration = duration //动画时间
        this.translateY = [0, 0, 0]
        this.status = 1    //抽奖状态 1.未开始 2.抽奖中
        this.delay = delay  // 延时时间
        this.lottery = []  //中奖列表
        this.height = 150  //单个高度
    }
    // 初始化页面
    init() {
        this.setShaftList()
        let domStr = ''
        this.shaftList.forEach(element => {
            domStr += `
                <div class="imgItem">
                    <img class="image" src="${element.img}" alt="${element.name}">
                </div>`
        })
        for (let i = 0; i < 3; i++) {
            listBoxs[i].innerHTML = domStr
        }
        // 获取抽奖按钮

        // 绑定抽奖事件
        startBtn.addEventListener('click', () => {
            this.start()
        })
    }
    // 开始抽奖
    start() {
        if (this.status == 2 || scoreObj.number == 0) return
        this.status = 2
        startBtn.classList.remove('btnAni')
        const objData = getResults()
        this.translateY = objData.arr
        this.translateY.forEach((item, index) => {
            for (let k = this.shaftList.length - 1; k >= 0; k--) {
                if (this.prizeList[item].value === this.shaftList[k].value) {
                    setTimeout(() => {
                        listBoxs[index].style.transitionDuration = `${this.duration - this.delay * 2}ms`
                        listBoxs[index].style.transform = `translateY(-${k * this.height}rem)`
                    }, index * this.delay)
                    break
                }
            }
        })
        // 抽奖结束
        setTimeout(() => {
            for (let i = 0; i < 3; i++) {
                listBoxs[i].style.transitionDuration = `0ms`
                listBoxs[i].style.transform = `translateY(-${this.translateY[i] * this.height}rem)`
            }
            if (objData.flag) {
                winDom.classList.add("winShow")
                scoreObj.add()
            } else {
                scoreObj.decrease()
            }
            startBtn.classList.add('btnAni')
            this.status = 1
            // let arr = this.translateY.map(item => {
            //     return this.prizeList[item].name
            // })
            // console.log('抽奖结果为：', arr)
        }, this.duration)
    }
    // 设置抽奖列表
    setShaftList() {
        let number = (this.duration - 1000) / 100
        let max = prizeList.length - 1;
        this.shaftList = [...this.prizeList]
        for (let i = 0; i <= number; i++) {
            this.shaftList.push(this.prizeList[Math.round(Math.random() * max)])
        }
        this.shaftList = [...this.shaftList, ...this.prizeList]
    }
}
// 调用类
const gameSlot = new Slot(prizeList)
gameSlot.init()


// 点击隐藏
winDom.addEventListener('click', function () {
    this.classList.remove("winShow")
})

// 分数类
class score {
    constructor(num) {
        this.number = num
        this.dom = document.querySelector('#scoreNum')
    }

    render() {
        this.dom.innerText = this.number
    }

    add() {
        this.number += 200
        this.render()
    }

    decrease() {
        if (this.number >= 100) {
            this.number -= 100
        } else {
            this.number = 0
        }
        this.render()
    }
}

const scoreObj = new score(1000)
scoreObj.render()