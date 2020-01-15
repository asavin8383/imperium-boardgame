<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
    <v-layout align-center justify-start column fill-height style="border: solid 0 blue">
        <v-container class="px-2 py admin-container">
            <v-layout class="mb-3" justify-start row>
                <v-card class="pa-3" style="width: 40%; min-width: 350px">
                    <v-flex class="mb-2 pr-2">
                        <v-layout class="lineText" row>
                            <v-flex align-self-center md6>
                                Режим функционирования системы
                            </v-flex>
                            <v-flex align-self-center md6>
                                {{ (funcMode)? funcMode.label : ""}}
                            </v-flex>
                        </v-layout>
                    </v-flex>
                </v-card>
            </v-layout>

            <v-layout class="mb-3">
                <v-card style="width: 100%">
                    <v-card-title class="pb-0">
                        <v-layout align-start justify-space-between row class="mb-3">
                            <!--                            <v-flex md6>
                                                            <v-layout align-start class="ml-2 pa-0" column>
                                                                <h3 class="ma-0 pa-0">{{title}}</h3>
                                                                <h5 v-if="!createMode" class="caption ma-0 pa-0">импортировано из ФГИС ППП "Реестр
                                                                    анонимайзеров"</h5>
                                                            </v-layout>
                                                        </v-flex>-->
                            <v-flex md10>
                                <v-tabs v-model="tabs" left height="40" slider-color="primary">
                                    <v-tab v-for="item in tabItems" :key="item.name" :disabled="item.disable">
                                        {{item.name}}
                                    </v-tab>
                                </v-tabs>
                            </v-flex>
                        </v-layout>
                    </v-card-title>
                    <v-card-text class="pt-0">
                        <v-flex class="pa-0">
                            <v-layout column class="tab-item-wrapper">
                                <v-tabs-items v-model="tabs" touchless>
                                    <v-tab-item touchless>
                                        <v-layout align-start justify-space-between row class="mb-3 mx-2">
                                            <v-flex md6 class="mr-3">
                                                <v-layout align-start class="ml-4" column>
                                                    <v-flex class="mb-2 pr-2 lineText">
                                                        <v-layout class="lineText" raw>
                                                            <v-flex align-self-center md6>
                                                                Общее состояние системы
                                                            </v-flex>
                                                            <v-flex align-self-center md6>
                                                                <span v-if="system_state !== null"><v-icon style="max-width: 40px" v-text="(system_state) ? 'far fa-check-circle': 'far fa-times-circle'" :color="(system_state) ? 'green' : 'red'"></v-icon></span>
                                                            </v-flex>
                                                        </v-layout>
                                                    </v-flex>
                                                    <v-flex class="mb-2 pr-2 lineText">
                                                        <v-layout class="lineText" raw>
                                                            <v-flex align-self-center md6>
                                                                API ППП "Реестр анонимайзеров"
                                                            </v-flex>
                                                            <v-flex align-self-center md6>
<!--                                                                <span>{{mainData.api_register | noData}}</span>-->
                                                                <span v-if="api_register !== null"><v-icon style="max-width: 40px" v-text="(api_register) ? 'far fa-check-circle': 'far fa-times-circle'" :color="(api_register) ? 'green' : 'red'"></v-icon></span>
                                                            </v-flex>
                                                        </v-layout>
                                                    </v-flex>
<!--                                                    <v-flex class="mb-2 pr-2 lineText">-->
<!--                                                        <v-layout class="lineText" raw>-->
<!--                                                            <v-flex align-self-center md6>-->
<!--                                                                API ППП "276"-->
<!--                                                            </v-flex>-->
<!--                                                            <v-flex align-self-center md6>-->
<!--                                                                <span>{{mainData.api_other | noData}}</span>-->
<!--                                                            </v-flex>-->
<!--                                                        </v-layout>-->
<!--                                                    </v-flex>-->
<!--                                                    <v-flex class="mb-2 pr-2 lineText">-->
<!--                                                        <v-layout class="lineText" raw>-->
<!--                                                            <v-flex align-self-center md6>-->
<!--                                                                Состояние КТС-->
<!--                                                            </v-flex>-->
<!--                                                            <v-flex align-self-center md6>-->
<!--                                                                <span>{{mainData.kts_state | noData}}</span>-->
<!--                                                            </v-flex>-->
<!--                                                        </v-layout>-->
<!--                                                    </v-flex>-->
                                                </v-layout>
                                            </v-flex>
                                            <v-flex md6 class="cube pa-0">
                                                <v-layout column align-end>
                                                    <v-btn class="ma-0 right mr-2 mb-2 admin-btn" color="primary"
                                                           @click="dialogChangeMode = !dialogChangeMode">
                                                        Изменить режим
                                                    </v-btn>
<!--                                                    <v-btn class="ma-0 right mr-2 my-2 admin-btn" color="primary"-->
<!--                                                           @click="">-->
<!--                                                        Открыть журнал событий-->
<!--                                                    </v-btn>-->
<!--                                                    <v-btn class="ma-0 right mr-2 mt-2 admin-btn" color="primary"-->
<!--                                                           @click="">-->
<!--                                                        Мониторинг микросервисов-->
<!--                                                    </v-btn>-->
                                                </v-layout>
                                            </v-flex>
                                        </v-layout>
                                        <v-layout align-start justify-space-between row class="mb-3 mx-2">
                                            <v-flex md12 class="mr-3">
                                                <div align="center" class="mt-3">
                                                    <h3>Состояние микросервисов</h3>
                                                </div>
                                                <v-data-table-custom
                                                        :headers="microservicesHeaders"
                                                        :items="microservices"
                                                        :pagination.sync="microservicesPagination"
                                                        :loading="msLoad"
                                                        class="elevation-1"
                                                >
                                                    <template v-slot:items="props">
                                                        <tr>
                                                            <td class="text-xs-left">{{ switchSubsystem(props.item.subsystem) | noData}}</td>
                                                            <td class="text-xs-left">{{ props.item.name | noData}}</td>
                                                            <td class="text-xs-left pl-0 pr-0">

                                                                <v-list>
                                                                    <v-list-group>
                                                                        <template v-slot:activator>
                                                                            <v-list-tile-content>
                                                                                <v-list-tile-title class="d-flex">
                                                                                    <v-icon class="pr-2" style="max-width: 40px" v-text="(checkGroupStatus(props.item.instances, props.item.expected_amount)) ? 'far fa-check-circle': 'far fa-times-circle'" :color="(checkGroupStatus(props.item.instances, props.item.expected_amount)) ? 'green' : 'red'"></v-icon>
                                                                                    <span>Количество экземпляров: {{props.item.instances.length}} из {{props.item.expected_amount}}</span>
                                                                                </v-list-tile-title>
                                                                            </v-list-tile-content>
                                                                        </template>
                                                                        <v-list disabled style="background: none" width="100%" >
                                                                            <v-list v-for="(inst, i) in props.item.instances" :key="i" class="d-flex align-center ml-5">
                                                                                <v-icon style="max-width: 40px" v-text="inst.icon" :color="inst.color"></v-icon>
                                                                                <v-list v-text="inst.id"></v-list>
                                                                            </v-list>
                                                                        </v-list>
                                                                    </v-list-group>
                                                                </v-list>

                                                            </td>
                                                            <td class="text-xs-center"><v-icon
                                                                    @click="refreshConfig(props.item.name)"
                                                                    color="green"
                                                                    title="Обновить конфигурацию микросервиса"
                                                                    :disabled="(funcMode)? funcMode.value !== 'SERVICE' : true"
                                                                >fas fa-share</v-icon></td>
                                                        </tr>
                                                    </template>
                                                    <template v-slot:no-data v-if="!msLoad">
                                                        <v-alert :value="true" color="warning" icon="warning">
                                                            Нет данных для отображения.
                                                        </v-alert>
                                                    </template>
                                                </v-data-table-custom>
                                            </v-flex>
                                        </v-layout>
                                    </v-tab-item>
                                    <v-tab-item touchless>
                                        <v-layout >
                                            <v-card flat class="px-3 pb-3 pt-0" style="width: 100%">
                                                <v-card-title class="pt-0">
                                                    <v-flex md6>
                                                        <v-text-field
                                                                v-model="search"
                                                                append-icon="search"
                                                                label="Найти"
                                                                single-line
                                                                hide-details
                                                                style="width: 50%"
                                                        ></v-text-field>
                                                    </v-flex>
                                                    <v-spacer></v-spacer>
                                                    <v-btn class="icon-btn" color="primary" @click="getReportsData">
                                                        <v-icon>refresh</v-icon>
                                                    </v-btn>
                                                    <v-btn :disabled="selected.length === 0" :loading="reloadReports" color="primary" @click="restartSeveralReports">Формировать</v-btn>
                                                </v-card-title>
                                                <v-data-table-custom
                                                        :headers="reportHeaders"
                                                        :items="indexedReportData"
                                                        v-model="selected"
                                                        item-key="id"
                                                        :search="search"
                                                        :pagination.sync="pagination"
                                                        :rows-per-page-items=pages
                                                        :loading="loadData"
                                                        class="mx-4"
                                                        select-all
                                                >
                                                    <template v-slot:headers="props">
                                                        <tr>
                                                            <th>
                                                                <v-checkbox
                                                                    :input-value="props.all"
                                                                    :indeterminate="props.indeterminate"
                                                                    color="primary"
                                                                    primary
                                                                    hide-details
                                                                    @click.stop="toggleAll"
                                                                ></v-checkbox>
                                                            </th>
                                                            <th
                                                                    v-for="header in props.headers"
                                                                    :key="header.text"
                                                                    :class="['column ', header.sortable ? 'sortable' : '', pagination.descending ? 'desc' : 'asc', header.value === pagination.sortBy ? 'active' : '', 'text-xs-' + header.align]"
                                                                    :style="{width: header.width, align: header.align}"
                                                                    @click="header.sortable ? changeSort(header.value) : ''"
                                                            >
                                                                {{ header.text }}
                                                                <v-icon v-if="header.sortable" small>arrow_upward</v-icon>
                                                            </th>
                                                        </tr>
                                                    </template>
                                                    <template v-slot:items="props">
                                                        <tr :active="props.selected" @click="props.selected = !props.selected">
                                                            <td class="text-xs-center" >
                                                                <v-checkbox
                                                                        :input-value="props.selected"
                                                                        primary
                                                                        color="primary"
                                                                        hide-details
                                                                ></v-checkbox>
                                                            </td>
                                                            <td class="text-xs-left">{{ props.item.repTpId | noData}}</td>
                                                            <td class="text-xs-left">{{ props.item.rep_nm | noData}}</td>
                                                            <td class="text-xs-center">{{ props.item.statuses.format | noData}}</td>
                                                            <td class="text-xs-center">{{ props.item.msr_prd_tp | noData}}</td>
                                                            <td class="text-xs-left">{{ props.item.msr_prd_caption | noData}}</td>
                                                            <td class="text-xs-center">{{ props.item.finish_dttm | dateTableFormat | noData}}</td>
                                                            <td class="text-xs-center">
                                                                <div style="display: flex; justify-content: center;">
                                                                    <v-icon class="icon-btn" :color="reloadReports ? '' : statusColor(props.item.statuses)"
                                                                            @click.stop="!reloadReports && fileStatus(props.item.statuses) !== 'RUNNING' ? restartReport(props.item) : ''">mdi-file-restore</v-icon>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </template>
                                                    <template v-slot:no-results>
                                                        <v-alert :value="true" color="warning" icon="warning">
                                                            /////
                                                        </v-alert>
                                                    </template>
                                                    <template v-slot:no-data v-if="!loadData">
                                                        <v-alert :value="true" color="warning" icon="warning">
                                                            /////
                                                        </v-alert>
                                                    </template>
                                                </v-data-table-custom>
                                            </v-card>
                                        </v-layout>
                                    </v-tab-item>
                                    <v-tab-item touchless>
                                        <v-layout align-start justify-space-between row class="mb-3 mx-2">
                                            <v-flex md6 class="mr-3">
                                                <v-layout align-start class="ml-4" column>

                                                    <v-flex class="mb-2 pr-2 lineText" v-for="(item, index) in systemParams" :key="(item.keyF) ? item.keyF : item.key">
                                                        <v-layout class="lineText" raw>
                                                            <v-flex align-self-center md4>
                                                                <span v-if="!item.keyF">{{item.key}}</span>
                                                                <v-text-field v-model="item.key" label="Введите ключ" v-if="editMode && item.keyF"
                                                                              class="areaTextSize"
                                                                ></v-text-field>
                                                            </v-flex>
                                                            <v-flex align-self-center md3 pl-3>
                                                                <span v-if="!editMode">{{item.value | noData}}</span>
                                                                <v-text-field v-model="item.value" label="Введите значение" v-if="editMode"
                                                                              class="areaTextSize"
                                                                ></v-text-field>
                                                            </v-flex>
                                                            <v-flex align-self-center md3 class="ml-3">
                                                                <v-combobox v-if="editMode"
                                                                        class="mt-1 pt-0"
                                                                        @input="item.configuration.id = $event.value; item.configuration.application = $event.text"
                                                                        :value="item.configuration.application"
                                                                        :items="globalConfigurations"
                                                                ></v-combobox>
                                                            </v-flex>
                                                            <v-flex align-self-center md2 v-if="editMode" class="pl-3">
                                                                <v-icon @click="removeSysParam(index)" color="primary">delete</v-icon>
                                                            </v-flex>
                                                        </v-layout>
                                                    </v-flex>

                                                    <v-flex class="mb-2 pr-2 lineText">
                                                        <v-layout class="lineText" raw>
                                                            <v-btn v-if="editMode" class="ma-0 right mr-2" color="primary" title="Добавить новый параметр"
                                                                   @click="addSysParam">
                                                                Добавить новый параметр
                                                            </v-btn>
                                                        </v-layout>
                                                    </v-flex>

                                                </v-layout>
                                            </v-flex>
                                            <v-flex md6 class="cube pa-0" v-if="!editMode">
                                                <v-btn v-if="!editMode" class="ma-0 right mr-2" color="primary" title="Редактировать"
                                                       @click="enableEditing">
                                                    Редактировать
                                                </v-btn>
                                            </v-flex>
                                        </v-layout>
                                        <v-card-actions v-if="editMode">
                                            <v-layout align-end row class="mb-3">
                                                <v-spacer></v-spacer>
                                                <v-btn flat class="ma-0 right mx-2" @click="cancelEditing">
                                                    Отмена
                                                </v-btn>
                                                <v-btn class="ma-0 right mx-2" color="primary" @click="applyEditing"><!--save-->
                                                    Сохранить
                                                </v-btn>
                                                <!--                                            <v-btn class="right mr-2" color="red" @click="dialogDelete = true">
                                                                                                Удалить
                                                                                            </v-btn>-->
                                            </v-layout>
                                        </v-card-actions>
                                    </v-tab-item>
                                    <v-tab-item touchless>
                                        <v-layout>
                                            <v-card flat class="px-3 pb-3 pt-0" style="width: 100%">
                                                <v-card-title class="pt-0">
                                                    <v-flex md6>
                                                        <v-text-field
                                                                v-model="robots.search"
                                                                append-icon="search"
                                                                label="Найти"
                                                                single-line
                                                                hide-details
                                                                style="width: 50%"
                                                        ></v-text-field>
                                                    </v-flex>
                                                    <v-spacer></v-spacer>
                                                    <v-btn class="icon-btn" color="primary" @click="getRobotsData">
                                                        <v-icon>refresh</v-icon>
                                                    </v-btn>
                                                </v-card-title>
                                                <v-data-table-custom
                                                        :headers="robots.headers"
                                                        :items="robots.data"
                                                        :pagination.sync="robots.pagination"
                                                        :total-items="robots.pagination.totalItems"
                                                        :rows-per-page-items=robots.pages
                                                        :loading="robots.loadData"
                                                        class="mx-4"
                                                        select-all
                                                >
                                                    <template v-slot:headers="props">
                                                        <tr>
                                                            <th
                                                                    v-for="header in props.headers"
                                                                    :key="header.text"
                                                                    :class="['column ', header.sortable ? 'sortable' : '', robots.pagination.descending ? 'desc' : 'asc', header.value === robots.pagination.sortBy ? 'active' : '', 'text-xs-' + header.align]"
                                                                    :style="{width: header.width, align: header.align}"
                                                                    @click="header.sortable ? changeSortRobots(header.value) : ''"
                                                            >
                                                                {{ header.text }}
                                                                <v-icon v-if="header.sortable" small>arrow_upward</v-icon>
                                                            </th>
                                                        </tr>
                                                    </template>
                                                    <template v-slot:items="props">
                                                        <tr :active="props.selected" @click="props.selected = !props.selected">
                                                            <td class="text-xs-left">{{ props.item.id | noData}}</td>
                                                            <td class="text-xs-left">{{ props.item.name | noData}}</td>
                                                            <td class="text-xs-center">{{ props.item.type | robotType | noData}}</td>
                                                            <td class="text-xs-center">{{ props.item.origId | sourceType | noData}}</td>
                                                            <td class="text-xs-left">{{ props.item.status | robotStatus | noData}}</td>
                                                            <td class="text-xs-center">{{ props.item.modificationDate | dateTableFormat | noData}}</td>
                                                            <td>
                                                                <div style="display: flex; justify-content: space-between; padding-right: 22%;">
                                                                    <v-icon class="icon-btn" color="primary" @click="editRobot(props.item)">edit</v-icon>
                                                                    <!--<v-icon class="icon-btn" color="primary" @click="deleteRecord(props.item)">delete</v-icon>-->
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </template>
                                                    <template v-slot:no-results>
                                                        <v-alert :value="true" color="warning" icon="warning">
                                                            <!--По запросу "{{ search }}" ничего не найдено.-->
                                                            Ничего не найдено.
                                                        </v-alert>
                                                    </template>
                                                    <template v-slot:no-data v-if="!loadData">
                                                        <v-alert :value="true" color="warning" icon="warning">
                                                            Нет данных для отображения.
                                                        </v-alert>
                                                    </template>
                                                </v-data-table-custom>
                                            </v-card>
                                        </v-layout>
                                    </v-tab-item>
                                </v-tabs-items>
                            </v-layout>
                        </v-flex>
                    </v-card-text>
                </v-card>
            </v-layout>
        </v-container>

        <v-dialog v-model="dialogChangeMode" max-width="600">
            <v-card class="pa-3">
                <v-card-title class="headline">Изменить режим функционирования системы</v-card-title>
                <v-card-text>
                    <v-radio-group v-model="modeChosen" row>
                        <v-radio
                                v-for="(m, idx) in modes"
                                :key="idx"
                                :label="m.label"
                                :value="m.value"
                        ></v-radio>
                    </v-radio-group>
                </v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn color="" flat @click="dialogChangeMode = false">Отмена</v-btn>
                    <v-btn color="primary"  @click="changeMode">ОК</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>

        <dialog-robot-ps v-model="robots.dialog.show" :id="robots.dialog.id" :action="robots.dialog.action" @changed="getRobotsData()"></dialog-robot-ps>

    </v-layout>
</template>

<script>
    import moment from 'moment';
    import * as VueNotifications from "vue-notifications";
    import CheckStatus from '../utils/utils'
    import DialogRobotPs from "../components/DialogRobotPs";

    export default {
        name: "AdministrationPage",
        components: {DialogRobotPs},

        data() {
            return {
                //main
                mainData: {},
                funcMode: null,
                dialogChangeMode: false,
                tabs: 0,
                tabItems: [{name: 'Общая информация', disable: false},
                    {name: 'Отчеты', disable: false},
                    {name: 'Параметры системы', disable: false},
                    {name: 'Конфигурирование роботов', disable: false},
                ],

                modeChosen: '',
                modes: [
                    { label: 'Штатный', value: 'NORMAL'},
                    { label: 'Сервисного обслуживания', value: 'SERVICE'},
                    { label: 'Аварийный', value: 'EMERGANCE'}
                ],

                //microservices
                microservicesHeaders: [
                    {text: 'Подсистема', align: 'left', width: '20%', value: 'subsystem'},
                    {text: 'Наименование', align: 'left', width: '15%', value: 'name'},
                    {text: 'Экземпляры', align: 'left', value: 'instances', sortable: false},
                    {text: 'Обновить конфигурацию', width: '10%', align: 'center', sortable: false}
                ],
                msLoad: false,
                microservicesPagination: {
                    ascending: true,
                    sortBy: "subsystem",
                    rowsPerPage: -1,
                    page: 1
                },

                microservices: [
                    {name: 'POD', subsystem: 'POD', expected_amount: 1, instances: []},
                    {name: 'PPT', subsystem: 'PPT', expected_amount: 1, instances: []},
                    {name: 'PPM', subsystem: 'PPM', expected_amount: 1, instances: []},
                    {name: 'DISPATCHER', subsystem: 'PMK', expected_amount: 1, instances: []},
                    {name: 'EXECUTOR', subsystem: 'PMK', expected_amount: 1, instances: []},
                    {name: 'ANALYZER', subsystem: 'PMK', expected_amount: 1, instances: []},
                    {name: 'REPORT_GENERATOR', subsystem: 'PPO', expected_amount: 1, instances: []},
                    {name: 'REPORT_SCHEDULER', subsystem: 'PPO', expected_amount: 1, instances: []},
                    {name: 'BIRT-VIEWER', subsystem: 'PPO', expected_amount: 1, instances: []},
                    {name: 'AUTH_SERVER', subsystem: 'SOIB', expected_amount: 1, instances: []},
                    {name: 'DISCOVERY', subsystem: 'SMUF', expected_amount: 1, instances: []},
                    {name: 'GATEWAY', subsystem: 'SMUF', expected_amount: 1, instances: []},
                    {name: 'CONFIG_SERVER', subsystem: 'SMUF', expected_amount: 1, instances: []}
                ],
                microservicesTimerId: null,

                //reports
                pagination: {
                    sortBy: 'repTpId',
                },
                search: '',
                pages: [10, 25, 50, 100],
                rowsPerPage: null,
                /*page: null,*/
/*                sortBy: null,
                descending: null,
                total: 0,*/
                selected: [],
                loadData: false,

                reportHeaders: [
                    {text: 'ID формы отчета', value: 'repTpId', width: '10%', align: 'left', sortable: true},
                    {text: 'Краткое название отчета', value: 'rep_nm', width: '20%', align: 'left', sortable: true},
                    {text: 'Формат отчета', value: 'format', width: '10%', align: 'center', sortable: true},
                    {text: 'Тип', value: 'msr_prd_tp', width: '20%', align: 'center', sortable: true},
                    {text: 'Период', value: 'msr_prd_caption', width: '20%', align: 'left', sortable: true},
                    {text: 'Дата и время последнего изменения', value: 'finish_dttm', width: '10%', align: 'center', sortable: true},
                    {text: 'Действия', value: 'actions', width: '10%', align: 'center', sortable: false}
                ],
                reportData: [],

                reloadReports: false,

                editMode: false,

                paramsData: {
                    etalon: {
                        proxy: {
                            password: null,
                            username: null,
                            host: null,
                            port: null,
                            type: null
                        },
                        enabled: true
                    },
                    nmap: {
                        path: null,
                        portsToCheck: null,
                        useProxy: false
                    },
                    totalWorkersCount: null,
                },

                robots: {
                    dialog: {
                        show: false,
                        id: null,
                        action: 'edit',
                    },
                    loading: false,
                    loadData: false,
                    search: "",
                    data: [],
                    pagination:{
                        sortBy: 'id'
                    },
                    pages: [10, 25, 50, 100],
                    headers: [
                        {text: 'ID', value: 'id', width: '10%', align: 'left', sortable: true},
                        {text: 'Наименование', value: 'name', width: '20%', align: 'left', sortable: true},
                        {text: 'Тип', value: 'type', width: '20%', align: 'center', sortable: true},
                        {text: 'Источник данных', value: 'origId', width: '20%', align: 'left', sortable: true},
                        {text: 'Статус', value: 'status', width: '20%', align: 'left', sortable: true},
                        {text: 'Дата и время последнего изменения', value: 'modificationDate', width: '10%', align: 'center', sortable: true},
                        {text: 'Действия', width: '10%', align: 'center', sortable: false}

                    ],
                },

                systemParams: [],
                copySystemParams: [],
                globalConfigurations: [],
                api_register: null,
                system_state: null,

            }
        },

        filters: {
            noData(v) {
                return (v == null || v === '') ? '-' : v;
            },

            sourceType(v) {
                return v === null ? null :
                       v < 0 ? 'АС 15.8' :
                       v > 0 ? 'ППП РА' :
                       null
            },

            robotStatus(v) {
                return v === null ? null :
                       v === 'WORK' ? 'работает' :
                       v === 'OUT_OF_WORK' ? 'не работает' :
                       null
            },

            robotType(v) {
                return v === null ? null :
                       v === 'PS' ? 'ПС' :
                       v === 'PASD' ? 'ПАСД' :
                       null
            },

            dateTableFormat(date) {
                return date != null ? moment(date).locale('ru').format('DD.MM.YY hh:mm') :
                       date;
            },

        },

        notifications: {
            showSuccessMsg: {
                type: VueNotifications.types.success,
                title: '',
                message: '',
            },
            showErrorMsg: {
                type: VueNotifications.types.error,
                title: '',
                message: '',
            },
            showInfoMsg: {
                type: VueNotifications.types.info,
                title: '',
                message: ''
            },
            showWarnMsg: {
                type: VueNotifications.types.warn,
                title: '',
                message: '',
            }
        },

        watch: {
            pagination: {
                handler(v) {
                    this.getReportsData();
                },
                deep: true
            },

            'robots.pagination': {
                handler(v) {
                    this.getRobotsData();
                },
                deep: true
            },

            search() {
                this.getReportsData();
            },

            'robots.search'() {
                this.getRobotsData();
            },
        },

        computed: {
            today() {
                return moment().locale('ru').format('hh:mm DD.MM.YYYY');
            },

            indexedReportData () {
                return this.reportData.map((item, index) => ({
                    id: index,
                    ...item
                }))
            }
        },

        mounted() {
            this.getMainInfo();
            this.getReportsData();
            this.getRobotsData();
        },

        updated() {

        },

        created() {
            this.getCurrentMode();
            this.getMicroservicesByInterval(10000);
            this.getSystemParams();
            this.getGlobalConfigurations();
            this.getApiRegister();
        },

        destroyed(){
            if(this.microservicesTimerId)
                clearInterval(this.microservicesTimerId);
        },

        methods: {
            getMainInfo() {
                /*this.$axios.get(this.$urls.ADMIN_STAT)
                    .then(resp => {
                        this.info = {
                            id: resp.data.rep_tp_id,
                            name: resp.data.shortName,
                            full_name: resp.data.fullName,
                            change_dtm: resp.data.updateDateTime
                        };
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    });*/
            },

            getCurrentMode(){
                this.$axios.post(this.$urls.ADMIN_CURRENT_SYSTEM_MODE)
                    .then(resp => {
                        this.funcMode = this.modes.filter(mode => {
                            return mode.value === resp.data;
                        })[0];
                    })
                    .catch(ex => {
                        if(CheckStatus.checkStatus403(ex, this.showWarnMsg))return;
                        if(CheckStatus.checkStatuses400(ex))
                            this.showWarnMsg({message: "Ошибка при получении режима работы системы"});
                        else
                            this.showErrorMsg({message: "Ошибка при получении режима работы системы"});
                    })
            },

            changeMode(){
                this.dialogChangeMode = false;
                this.$axios.post(this.$urls.ADMIN_CHANGE_SYSTEM_MODE, {systemMode: this.modeChosen})
                    .then(() => {
                        this.funcMode = this.modes.filter(mode => {
                            return mode.value === this.modeChosen;
                        })[0];
                        this.showSuccessMsg({message: "Режим работы системы успешно сменен"});
                    })
                    .catch(ex => {
                        if(CheckStatus.checkStatus403(ex, this.showWarnMsg))return;
                        if(CheckStatus.checkStatuses400(ex))
                            this.showWarnMsg({message: "Ошибка при смене режима работы системы"});
                        else
                            this.showErrorMsg({message: "Ошибка при смене режима работы системы"});
                    })
            },

            goToEventJournals() {
                //window.open(this.$urls.ADMIN_EVENT_JOURNALS);
            },

            getMicroservicesByInterval(interval) {
                this.getMicroservices();
                this.microservicesTimerId = setInterval(() => {
                    this.getMicroservices();
                }, interval);
            },

            getMicroservices() {
                this.msLoad = true;
                this.$axios.get(
                        this.$urls.ADMIN_MICROSERVICES,
                        {headers: {Accept: 'application/json'}})
                    .then(resp => {
                        this.system_state = true;
                        let apps = resp.data.applications.application;
                        //console.log(apps);
                        apps.forEach(app => {
                            let instances = [];
                            let subsystem = '';
                            let expected_amount = 1;
                            app.instance.forEach(inst => {
                                let icon = 'far fa-arrow-alt-circle-down';
                                let color = 'red'
                                if(inst.status === 'UP') {
                                    icon = 'far fa-check-circle';
                                    color = 'green';
                                }else{
                                    this.system_state = false;
                                }
                                if('metadata' in inst){
                                    if('subsystem' in inst.metadata)
                                        subsystem = inst.metadata.subsystem;
                                    if('expectedAmount' in inst.metadata)
                                        expected_amount = inst.metadata.expectedAmount;
                                }
                                instances.push({
                                    id: inst.instanceId,
                                    status: inst.status,
                                    icon,
                                    color
                                });
                            });
                            if(expected_amount.toString() !== instances.length.toString())
                                this.system_state = false;
                            this.microservices.forEach(ms => {
                                if (ms.name === app.name) {
                                    ms.name = app.name;
                                    ms.instances = instances;
                                    ms.subsystem = subsystem;
                                    ms.expected_amount = expected_amount;
                                }
                            });
                        });
                    }).catch(e => {
                        console.log('error ', e);
                    })
                    .finally(() => {
                        this.msLoad = false;
                        if(this.system_state !== false)
                            this.system_state = true;
                    });
            },

            switchSubsystem(subsystem) {
                switch(subsystem) {
                    case 'SOIB':
                        return 'Подсистема обеспечения безопасности';
                    case 'SMUF':
                        return 'Подсистема монитринга и управления';
                    case 'POD':
                        return 'Подсистема обмена данными';
                    case 'PPT':
                        return 'Подсистема подготовки трафика';
                    case 'PPM':
                        return 'Подсистема подготовки мероприятий';
                    case 'PPO':
                        return 'Подсистема подготовки отчетов';
                    case 'PMK':
                        return 'Подсистема проведения мероприятий по контролю ПС/ПАСД';
                    default:
                        return '-'
                }
            },

            refreshConfig(msName){
                this.$axios.post(this.$urls.ADMIN_REFRESH_MS_CONFIG + msName.toLowerCase())
                    .then(() => {
                        this.showSuccessMsg({message: "Обновление конфигурации успешно запущено"});
                    }).catch(e => {
                        this.showErrorMsg({message: "Ошибка запуска обновления конфигурации" + e});
                    });
            },

            getReportsData() {
                this.loadData = true;

                this.$axios.get(this.$urls.ADMIN_REPORTS_TABLE)
                    .then(resp => {
                        this.reportData = resp.data;
                        let resArray = [];
                        resp.data.forEach(group => {
                            let obj = Object.assign({}, group);
                            group.statuses.forEach(m => {
                                let temp = Object.assign({}, obj);
                                temp.statuses = Object.assign({}, m);
                                resArray.push(temp);
                            });
                        });
                        this.reportData = resArray;
                        //this.total = resp.data.totalElements;
                        //this.pagination.totalItems = resp.data.totalElements;
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    })
                    .finally(() => {
                        this.loadData = false;
                    });
            },

            getRobotsData() {
                this.robots.loadData = true;

                let params = {
                    pageSize: this.robots.pagination.rowsPerPage,
                    pageNumber: this.robots.pagination.page - 1,
                    sortingColumn: this.robots.pagination.sortBy,
                    sortingDirection: this.sortTable(this.robots.pagination.descending),
                    query: (this.robots.search || "").trim()
                };
                this.$axios.get(this.$urls.ADMIN_ROBOTS_TABLE, {params})
                    .then(resp => {
                        this.robots.data = resp.data.content;
                        this.robots.pagination.totalItems = resp.data.totalElements;
                    })
                    .catch(e => {
                        console.log('error: ', e);
                    })
                    .finally(() => {
                        this.robots.loadData = false;
                    });
            },

            createRobotConfiguration () {
            },

            editRobot(item) {
                this.robots.dialog.id = item.id;
                this.robots.dialog.action = 'edit';
                this.robots.dialog.show = true;
            },

            sortTable(descending) {
                if (descending != null) {
                    if (descending)
                        return 'DESC';
                    else return 'ASC'
                } else return descending;
            },

            changeSort (column) {
                if (this.pagination.sortBy === column) {
                    this.pagination.descending = !this.pagination.descending
                } else {
                    this.pagination.sortBy = column;
                    this.pagination.descending = false
                }
            },

            changeSortRobots (column) {
                if (this.robots.pagination.sortBy === column) {
                    this.robots.pagination.descending = !this.robots.pagination.descending
                } else {
                    this.robots.pagination.sortBy = column;
                    this.robots.pagination.descending = false
                }
            },

            toggleAll () {
                if (this.selected.length) this.selected = [];
                else this.selected = this.indexedReportData.slice()
            },

            restartReport(item) {
                console.log("restarting report id " + item.statuses.rep_id);
                this.$axios.post(this.$urls.ADMIN_RESTART_REPORT + '/' + item.statuses.rep_id)
                    .then(() => {
                        this.showSuccessMsg({message: "Отчет перезагружен"});
                        console.log("Отчет перезагружен")
                    })
                    .catch(e => {
                        console.log('error ', e);
                        if(CheckStatus.checkStatus403(e, this.showWarnMsg))return;
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: "Ошибка перезагрузки отчета"});
                        else
                            this.showErrorMsg({message: "Ошибка перезагрузки отчета"});
                    })
                    .finally(() => {
                        this.getReportsData();
                    });
            },

            restartSeveralReports() {
                console.log("restarting several reports " + this.selected.length);
                this.reloadReports = true;
                let requests = [];
                this.selected.forEach((report) => {
                    console.log("id: " + report.statuses.rep_id);
                    requests.push(this.$axios.post(this.$urls.ADMIN_RESTART_REPORT + '/' + report.statuses.rep_id))
                });

                Promise.all(requests)
                    .then(() => {
                        this.showSuccessMsg({message: "Отчеты перезагружены"});
                        console.log("Отчеты перезагружены")
                    })
                    .catch(e => {
                        console.log('error ', e);
                        if(CheckStatus.checkStatus403(e, this.showWarnMsg))return;
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: "Ошибка перезагрузки отчета"});
                        else
                            this.showErrorMsg({message: "Ошибка перезагрузки отчета"});
                    })
                    .finally(() => {
                        this.selected = [];
                        this.reloadReports = false;
                        this.getReportsData();
                    })
            },

            fileStatus(statuses) {
                return statuses.status;
            },

            statusColor(statuses) {
                switch(this.fileStatus(statuses)) {
                    case 'NEW':
                        return '#1DB340';
                    case 'RUNNING':
                        return '#F5BB47';
                    case 'OK':
                        return 'primary';
                    case 'FAILED':
                        return '#E55949';
                    default:
                        return ''
                }
            },

            getSystemParams() {
                this.$axios.post(this.$urls.GLOBAL_PROPERTIES)
                    .then((resp) => {
                        this.systemParams = resp.data;
                    })
                    .catch(e => {
                        console.log('error ', e);
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: "Ошибка"});
                        else
                            this.showErrorMsg({message: "Ошибка"});
                    })
            },
            getGlobalConfigurations() {
                this.$axios.post(this.$urls.GLOBAL_CONFIGURATIONS)
                    .then((resp) => {
                        resp.data.forEach((elem) => {
                            this.globalConfigurations.push({text: elem.application, value: elem.id});
                        });
                    })
                    .catch(e => {
                        console.log('error ', e);
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: "Ошибка"});
                        else
                            this.showErrorMsg({message: "Ошибка"});
                    })
            },
            enableEditing() {
                this.systemParams.forEach(elem => {
                    let configuration = Object.assign({}, elem.configuration);
                    this.copySystemParams.push(Object.assign({}, elem, {configuration}));
                });
                this.editMode = !this.editMode;
            },
            cancelEditing() {
                this.systemParams = [];
                this.copySystemParams.forEach(elem => {
                    this.systemParams.push(Object.assign({}, elem));
                });
                this.copySystemParams = [];
                this.editMode = !this.editMode;
            },
            applyEditing() {
                let arrForRequest = [];
                this.systemParams.forEach(elem => {
                    let configuration = Object.assign({}, elem.configuration);
                    let newElem = Object.assign({}, elem, {configuration});
                    if(newElem.keyF)
                        delete newElem.keyF;
                    arrForRequest.push(newElem);
                });
                this.$axios.put(this.$urls.GLOBAL_PROPERTIES, arrForRequest)
                    .then(() => {
                        this.systemParams = arrForRequest;
                        this.copySystemParams = [];
                        this.editMode = !this.editMode;
                    })
                    .catch(e => {
                        console.log('error ', e);
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: "Ошибка"});
                        else
                            this.showErrorMsg({message: "Ошибка"});
                    });
            },
            removeSysParam(index) {
                this.systemParams.splice(index, 1);
            },
            addSysParam() {
                let keyF = (this.systemParams[this.systemParams.length - 1].keyF) ? this.systemParams[this.systemParams.length - 1].keyF + 1 : 111000;
                this.systemParams.push({key: '', value: '', "configuration": {"id": '', "application": ""}, keyF});
            },

            getApiRegister() {
                this.$axios.get(this.$urls.PPPRACHECK)
                    .then((resp) => {
                        this.api_register = resp.data;
                    })
                    .catch(e => {
                        console.log('error ', e);
                        if(CheckStatus.checkStatuses400(e))
                            this.showWarnMsg({message: "Ошибка"});
                        else
                            this.showErrorMsg({message: "Ошибка"});
                    });
            },

            checkGroupStatus(items, expected_amount){
                let status = true;
                items.forEach((elem) => {
                    if(elem.status !== "UP")
                        status = false;
                });
                if(expected_amount.toString() !== items.length.toString())
                    status = false;
                return status;
            }
        },
    }
</script>

<style scoped>
    .text-style {
        text-align: center;
    }

    .lineText {
        width: 100%;
        height: 45px;
    }

</style>
<style>
    .icon-btn {
        padding: 3px !important;
        height: auto !important;
        cursor: pointer;
    }
    .icon-btn.v-btn {
        min-width: 0 !important;
    }
    .icon-btn .v-icon {
        font-size: 30px;
    }
    .admin-btn {
        width: 40%
    }

    .admin-container  .v-tabs__slider-wrapper .v-tabs__slider {
        height: 3px;
        width: 100%;
    }
</style>
