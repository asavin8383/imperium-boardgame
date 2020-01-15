<template>

    <div class="el">
        <div ref="slider" class="mx-0 style-slider">
            <template v-for="i in 24">
                <div :class="i!==24? 'hour':'last-hour'" :key="`slider-${i-1}`" :style="{width: blockWidth}">
                    <div class="half-hour"></div>
                </div>
            </template>
            <vue-drag-resize is-active
                             prevent-active-behavior
                             :h="50"
                             axis="x"
                             :sticks="['ml', 'mr']"
                             :class="{'transition-effect1': !isDragginig}"
                             :stick-icon="true"
                             :parentLimitation="true"
                             isDraggable
                             isResizable
                             :x="left * pxByMin"
                             :w="width * pxByMin"
                             :minw="pxByMin"
                             :parentW="clientWidth"
                             @resizing="onResizing($event)"
                             @resizestop="onResizeStop()"
                             @dragging="onDragging($event)"
                             @dragstop="onDragStop()"
                             v-on:dblclick.native="counter += 1, dblClick($event)"

            >
                <div class="slider"></div>
            </vue-drag-resize>
            <v-menu v-if="info_proxy" v-model="info_proxy" z-index="100" absolute :position-x="x" :position-y="y" :close-on-content-click=false>
                <v-card style="width: 500px;">
                    <v-card-title class="pb-0">
                        <v-layout align-start justify-space-between row class="mb-2">
                            <v-flex md4>
                                <v-layout align-center class="ml-2 pa-0" column>
                                    <v-flex style="width: 100%">
                                        <span class="blue--text">{{id}}</span>
                                    </v-flex>
                                </v-layout>
                            </v-flex>
                            <v-flex md8 class="pa-0">
                                <v-layout raw class="justify-end">
                                    <v-btn class="pa-2" color="primary" @click="stopEvent" :disabled="status !== 'RUNNING' && !stop">Остановить</v-btn>
<!--                                    <v-btn class="pa-2" color="primary" @click="stop = !stop" v-if="stop&&!end">Возобновить</v-btn>-->
<!--                                    <v-btn class="pa-2" color="primary" @click="end = !end" v-if="stop&&!end">Завершить</v-btn>-->
                                </v-layout>
                            </v-flex>
                        </v-layout>
                    </v-card-title>
                    <v-card-text>
                        <v-layout align-start justify-space-between row class="mb-2">
                            <v-flex md6>
                                <v-layout align-start class="ml-2 pa-0" column>
                                    <v-flex style="width: 100%"> Наименование: </v-flex>
                                </v-layout>
                            </v-flex>
                            <v-flex md6 class="pa-0">
                                <v-layout align-start class="ml-2 pa-0" column>
                                    <v-flex style="width: 100%"><span>{{title}}</span></v-flex>
                                </v-layout>
                            </v-flex>
                        </v-layout>
                        <v-layout align-start justify-space-between row class="mb-2">
                            <v-flex md6>
                                <v-layout align-start class="ml-2 pa-0" column>
                                    <v-flex style="width: 100%"> Выполнить до: </v-flex>
                                </v-layout>
                            </v-flex>
                            <v-flex md6 class="pa-0">
                                <v-layout align-start class="ml-2 pa-0" column>
                                    <v-flex style="width: 100%"><span>{{targetItem.plannedEndTime}}</span></v-flex>
                                </v-layout>
                            </v-flex>
                        </v-layout>
                        <v-layout align-start justify-space-between row class="mb-2">
                            <v-flex md6>
                                <v-layout align-start class="ml-2 pa-0" column>
                                    <v-flex style="width: 100%"> Продолжительность проверки: </v-flex>
                                </v-layout>
                            </v-flex>
                            <v-flex md6 class="pa-0">
                                <v-layout align-start class="ml-2 pa-0" column>
                                    <v-flex style="width: 100%"><span>{{getCheckDuration}}</span></v-flex>
                                </v-layout>
                            </v-flex>
                        </v-layout>
                        <v-layout align-start justify-space-between row class="mb-2">
                            <v-flex md6>
                                <v-layout align-start class="ml-2 pa-0" column>
                                    <v-flex style="width: 100%"> Время: </v-flex>
                                </v-layout>
                            </v-flex>
                            <v-flex md6 class="pa-0">
                                <v-layout align-start justify-space-between row class="ml-2 pa-0">
                                    <v-flex md6 style="">
                                        <v-text-field
                                            class="pt-0 mt-0"
                                            hint="время от"
                                            return-masked-value
                                            mask="##:##"
                                            persistent-hint
                                            v-model="timeFrom"
                                        ></v-text-field>
                                    </v-flex>
                                    <v-flex md6 style="">
                                        <v-text-field
                                            class="pt-0 mt-0 pl-2"
                                            hint="время до"
                                            return-masked-value
                                            mask="##:##"
                                            persistent-hint
                                            v-model="timeTo"
                                        ></v-text-field>
                                    </v-flex>
                                </v-layout>
                            </v-flex>
                        </v-layout>
                    </v-card-text>
                </v-card>
            </v-menu>
            <div class="slider-label"
                 :style="{left: (left * pxByMin) < (clientWidth / 2) ? (5 + (left * pxByMin)) + 'px' : ((left * pxByMin) - 95) + 'px'}">
                <span v-if="isDragginig" class="minutes elevation-2">c {{resultLeft((left * pxByMin))}} по {{resultRight((left * pxByMin),(width * pxByMin))}}</span>
            </div>
        </div>
    </div>

</template>

<script>
    import VueDragResize from 'vue-drag-resize';
    import Random from 'random-seed';
    import * as VueNotifications from "vue-notifications";
    import CheckStatus from '../utils/utils'

    import moment from 'moment';

    // noinspection JSUnusedGlobalSymbols
    export default {
        name: "schedule-stripe",

        props: ['id', 'title', 'info', 'left', 'width', 'is-dragginig', 'client-width', 'block-width', 'status', 'progress', 'position', 'targetItem'],

        components: {
            VueDragResize
        },

        data() {
            return {
                x: 0,
                y: 0,
                counter: 0,
                stop: true,
                end: false,
                progressPercent: null,
                progressTimerId: null
            }
        },

        computed: {
            pxByMin() {
                return this.clientWidth / 1439;
            },
            info_proxy: {
                get() {return this.info},
                set(v) {this.$emit('update:info', v);}
            },
            getCheckDuration() {
                return moment.utc(moment(this.targetItem.plannedEndTime, "HH:mm:ss").diff(moment(this.targetItem.plannedStartTime, "HH:mm:ss"))).format("HH:mm:ss");
            },

            timeFrom: {
                get() {
                    return this.resultLeft((this.left * this.pxByMin));
                },
                set(v) {
                    let newValue = "";
                    let time = v.split(":");
                    if(!time[0] || !time[1]) return;
                    if(time[0].length <= 1 || time[1].length <= 1) return;

                    newValue += (+time[0] > 23) ? "23" : moment(time[0], "HH").format("HH");
                    newValue += ":";
                    newValue += (+time[1] > 59) ? "59" : moment(time[1], "mm").format("mm");
                    this.targetItem.left = this.setLeft(newValue);
                }
            },

            timeTo: {
                get() {
                    return this.resultRight((this.left * this.pxByMin), (this.width * this.pxByMin));
                },
                set(v) {
                    let newValue = "";
                    let time = v.split(":");
                    if(!time[0] || !time[1]) return;
                    if(time[0].length <= 1 || time[1].length <= 1) return;

                    newValue += (+time[0] > 23) ? "23" : moment(time[0], "HH").format("HH");
                    newValue += ":";
                    newValue += (+time[1] > 59) ? "59" : moment(time[1], "mm").format("mm");
                    this.targetItem.width = this.setWidth(this.resultLeft((this.left * this.pxByMin)), newValue);
                }
            }

        },

        created() {
            this.getProgressPercent();
            this.bubbleData();
        },

        destroyed(){
            if(this.progressTimerId)
                clearInterval(this.progressTimerId);
        },

        notifications: {
            showWarnMsg: {
                type: VueNotifications.types.warn,
                title: '',
                message: '',
            }
        },

        methods: {

            onResizing(event) {
                this.$emit('update:is-dragginig', true);
                this.$emit('update:left', event.left / this.pxByMin);
                this.$emit('update:width', event.width / this.pxByMin | 1);
                this.$emit('update:info', false);
            },

            onResizeStop() {
                this.$emit('update:is-dragginig', false);
                this.$emit('update:info', false);
            },

            onDragging(event) {
                this.$emit('update:is-dragginig', true);
                this.$emit('update:left', event.left / this.pxByMin);
                this.$emit('update:info', false);
            },

            onDragStop() {
                this.$emit('update:is-dragginig', false);
                this.$emit('update:info', false);
            },

            resultLeft(left) {
                const min_from = Math.round(left / this.pxByMin);
                let h = min_from / 60;
                let m = min_from % 60;
                return moment().hours(h).minutes(m).seconds(0).format("HH:mm:00");
            },

            resultRight(left, width) {
                const min_to = Math.round((width + left) / this.pxByMin);
                return moment().hours(min_to / 60).minutes(min_to % 60).seconds(0).format("HH:mm:00");
            },

            dblClick(event) {
                this.x = event.x;
                this.y = event.y;
                this.$emit('update:info', true);
                this.counter = 0;
            },

            getProgressPercent() {
                if(this.status === 'FINISHED') this.progressPercent = 100;
                if(this.status === 'PLANNED') this.progressPercent = 0;

                this.getProgress();
                this.progressTimerId = setInterval(() => {
                    this.getProgress();
                }, 10000);
            },

            getProgress() {
                this.$axios.get(this.$urls.SHEDULE_PROGRESS2, {params: {id: this.id}}).then(resp => {
                    this.progressPercent = resp.data;
                    this.bubbleData();
                }).catch(e => {
                    // console.log('error ', e);
                    // if (e.data){
                    //     if(CheckStatus.checkStatuses400(e))
                    //         this.showWarnMsg({message: e.data.message});
                    //     else
                    //         this.showErrorMsg({message: e.data.message});
                    // }
                });
            },

            bubbleData() {
                this.$emit('stripeData', {
                    id: this.id,
                    title: this.title,
                    progressPercent: this.progressPercent,
                    position: this.position,
                });
            },

            setLeft(start) {
                return moment.duration(start).asMinutes();
            },

            setWidth(start, end) {
                return moment.duration(end).asMinutes() - moment.duration(start).asMinutes();
            },

            stopEvent() {
                this.$axios.put(this.$urls.STOP_RUNNING_EVENT + "?id=" + this.id).then(resp => {
                    if(resp !== false)
                        this.stop = false;
                }).catch(e => {
                    console.log('error ', e);
                    if (e.data){
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: e.data.message});
                        else
                            this.showErrorMsg({message: e.data.message});
                    }
                });
            }

        },
    }
</script>

<style scoped>


	.el {
		height: 50px;
		font-size: 12px;
		line-height: 50px;
		font-family: Helvetica,serif;
		border-bottom: 1px solid #aaaaaa;
		cursor: default;
	}

	.style-num {
		width: 30px;
		display: inline-block;
		height: inherit;
		text-align: right;
		vertical-align: top;
	}


	.name-title {
		width: 85px;
		display: inline-block;
		text-align: center;
		height: inherit;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

    .name-progress {
		width: 35px;
		display: inline-block;
		text-align: center;
		height: inherit;
		border-right: 2px solid #d6d6d6;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.style-hours div {
		display: inline-block;
		text-align: left;
	}

	.style-slider {
		width: 100%;
		display: inline-block;
		height: 49px;
		position: absolute;
		text-align: start;
	}

    .hour {
        display: inline-block;
        border-right: 1px solid #d6d6d6;
        height: calc(100% - 1px);;
    }

    .last-hour {
        display: inline-block;
        height: calc(100% - 1px);;
    }

    .half-hour {
        display: inline-block;
        border-right: 1px solid #f7f6f6;
        height: calc(100% - 1px);
        width: calc(100% / 2);
    }

    .slider {
		width: 100%;
		height: 100%;
		background-color: rgba(66, 139, 202, 0.5);
		border-left: 2px solid #428bca;
	}

	.slider-label {
		position: absolute;
		top: -5px;
		padding: 3px;
		border-radius: 3px;
	}

	.minutes {
		white-space: nowrap;
		position: absolute;
		background: rgba(255, 255, 255, 1);
		border: solid thin black;
		padding: 0 5px;
		top: -30px;
		left: -1px;
		z-index: 10000;
	}


</style>

<style lang="scss">

	$handle_width: 9px;
	$handle_height: 19px;

	$handle_offset_x: 1 - $handle_width;
	$handle_offset_y: 0 - $handle_height / 2;

	.vdr-stick {
		width: $handle_width !important;
		height: $handle_height !important;
		margin-top: $handle_offset_y !important;
		background-color: rgba(255, 255, 255, 0.3);
		border: double thin gray;
		overflow: hidden;

	.v-icon:before {
		position: relative;
		left: -6px;
		top: -3px;
		font-size: 24px;
	}
	}

	.vdr-stick-ml {
		left: $handle_offset_x !important;
	}

	.vdr-stick-mr {
		right: $handle_offset_x !important;
	}


</style>